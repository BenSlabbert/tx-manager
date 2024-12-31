/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.annotation.Transactional;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice.Thrown;
import org.slf4j.Logger;

class RequiresNewAdvice {

  private RequiresNewAdvice() {}

  @OnMethodEnter
  private static void onEnter(
      @AllArguments(nullIfEmpty = true) Object[] args,
      @Origin Method method,
      @Origin("#m") String methodName,
      @FieldValue(value = "log") Logger log) {

    // TODO
    //  it would be great to have the annotation values determined during compilation rather than
    //  later during runtime
    Transactional annotation =
        Objects.requireNonNull(
            method.getAnnotation(Transactional.class),
            "method must have the @Transactional annotation");

    if (log.isDebugEnabled()) {
      log.debug("Entering advised method: {}", methodName);
      log.debug("propagation: {}", annotation.propagation());
      log.debug("doNotRollBackFor: {}", Arrays.toString(annotation.doNotRollBackFor()));
      log.debug("args: {}", args == null ? "null" : Arrays.toString(args));
      log.debug("begin transaction method: {}", methodName);
    }

    PlatformTransactionManager.begin();
  }

  @OnMethodExit(onThrowable = Exception.class)
  private static void onExit(
      @Origin Method method,
      @Origin("#m") String methodName,
      @Thrown(readOnly = false) Throwable throwable,
      @FieldValue(value = "log") Logger log) {

    // TODO
    //  it would be great to have the annotation values determined during compilation rather than
    //  later during runtime

    Transactional annotation =
        Objects.requireNonNull(
            method.getAnnotation(Transactional.class),
            "method must have the @Transactional annotation");

    log.debug("Exiting advised method: {}", methodName);

    if (throwable != null) {
      log.error("Exception thrown in advised method: {}", methodName, throwable);
      for (Class<?> clazz : annotation.doNotRollBackFor()) {
        if (throwable.getClass().equals(clazz)) {
          log.error(
              "Transaction will not rollback as thrown exception {} matches: {}",
              throwable.getClass().getCanonicalName(),
              clazz.getCanonicalName());
          PlatformTransactionManager.commit();
          // set this to null otherwise it will be thrown later
          throwable = null;
          return;
        }
      }
      PlatformTransactionManager.rollback();
      return;
    }

    log.debug("commit after method: {}", methodName);
    PlatformTransactionManager.commit();
  }
}
