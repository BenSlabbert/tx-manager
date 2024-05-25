/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;

import github.benslabbert.txmanager.annotation.TimerAdvice;
import java.util.Arrays;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.Local;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice.Thrown;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import org.slf4j.Logger;

public class TimerAdivcePlugin implements Plugin {

  @Override
  public DynamicType.Builder<?> apply(
      DynamicType.Builder<?> builder,
      TypeDescription typeDescription,
      ClassFileLocator classFileLocator) {

    // https://github.com/raphw/byte-buddy/issues/893
    // try to use advice as a decorator instead of an implementation
    // this wraps around the original method without change
    return builder.visit(
        Advice.to(TimerAdviceImpl.class)
            .on(isMethod().and(isAnnotatedWith(named(TimerAdvice.class.getCanonicalName())))));
  }

  @Override
  public void close() {
    // nothing open
  }

  @Override
  public boolean matches(TypeDescription typeDefinitions) {
    return typeDefinitions.getDeclaredMethods().stream()
        .anyMatch(f -> f.getDeclaredAnnotations().isAnnotationPresent(TimerAdvice.class));
  }

  private static final class TimerAdviceImpl {

    @OnMethodEnter
    private static void onEnter(
        @Local("time") long time,
        @AllArguments(nullIfEmpty = true) Object[] args,
        @Origin("#m") String methodName,
        @FieldValue(value = "log") Logger log) {

      log.debug("Entering advised method: {}", methodName);
      log.debug("args: {}", args == null ? "null" : Arrays.toString(args));
      time = System.currentTimeMillis();
    }

    @OnMethodExit(onThrowable = Exception.class)
    private static void onExit(
        @Local("time") long time,
        @Origin("#m") String methodName,
        @Thrown Throwable throwable,
        @FieldValue(value = "log") Logger log) {

      log.info("Exiting advised method: {}", methodName);
      long total = System.currentTimeMillis() - time;
      log.info("execution time: {}ms", total);
    }
  }
}
