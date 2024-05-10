/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.annotation.Transactional;
import java.util.Arrays;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice.Thrown;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import org.slf4j.Logger;

public class TransactionalAdvicePlugin implements Plugin {

  @Override
  public DynamicType.Builder<?> apply(
      DynamicType.Builder<?> builder,
      TypeDescription typeDescription,
      ClassFileLocator classFileLocator) {

    return builder
        .method(isAnnotatedWith(named(Transactional.class.getCanonicalName())))
        .intercept(Advice.to(TryFinallyAdvice.class));
  }

  @Override
  public void close() {
    // nothing open
  }

  @Override
  public boolean matches(TypeDescription typeDefinitions) {
    AnnotationList declaredAnnotations = typeDefinitions.getDeclaredAnnotations();
    return declaredAnnotations.isAnnotationPresent(Transactional.class);
  }

  private static class TryFinallyAdvice {

    private TryFinallyAdvice() {}

    @OnMethodEnter
    private static void onEnter(
        @AllArguments Object[] args,
        @Origin("#m") String methodName,
        @FieldValue(value = "log") Logger log) {

      log.debug("Entering advised method: {}", methodName);
      log.debug("args: {}", args == null ? "null" : Arrays.toString(args));

      PlatformTransactionManager.begin();
    }

    @OnMethodExit(onThrowable = Exception.class)
    private static void onExit(
        @Origin("#m") String methodName,
        @Thrown Throwable throwable,
        @FieldValue(value = "log") Logger log) {

      log.info("Exiting advised method: {}", methodName);

      if (throwable != null) {
        log.error("Exception thrown in advised method: {}", methodName, throwable);
        PlatformTransactionManager.rollback();
        return;
      }

      PlatformTransactionManager.commit();
    }
  }
}
