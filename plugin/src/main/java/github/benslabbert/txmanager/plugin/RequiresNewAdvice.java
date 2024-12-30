/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.PlatformTransactionManager;
import java.util.Arrays;
import net.bytebuddy.asm.Advice;
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
      @Origin("#m") String methodName,
      @FieldValue(value = "log") Logger log) {

    if (log.isDebugEnabled()) {
      log.debug("Entering advised method: {}", methodName);
      log.debug("args: {}", args == null ? "null" : Arrays.toString(args));
    }

    log.debug("begin transaction method: {}", methodName);
    PlatformTransactionManager.begin();
  }

  @OnMethodExit(onThrowable = Exception.class)
  private static void onExit(
      @Origin("#m") String methodName,
      @Thrown Throwable throwable,
      @FieldValue(value = "log") Logger log) {

    log.debug("Exiting advised method: {}", methodName);

    if (throwable != null) {
      log.error("Exception thrown in advised method: {}", methodName, throwable);
      PlatformTransactionManager.rollback();
      return;
    }

    log.debug("commit after method: {}", methodName);
    PlatformTransactionManager.commit();
  }
}
