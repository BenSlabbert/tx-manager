/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.PlatformTransactionManager;
import java.util.Arrays;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;

public class RequiresNewAdvice {

  private RequiresNewAdvice() {}

  @Advice.OnMethodEnter
  private static void onEnter(
      @Advice.AllArguments(nullIfEmpty = true) Object[] args,
      @Advice.Origin("#m") String methodName,
      @Advice.FieldValue(value = "log") Logger log) {

    log.debug("Entering advised method: {}", methodName);
    log.debug("args: {}", args == null ? "null" : Arrays.toString(args));

    PlatformTransactionManager.begin();
  }

  @Advice.OnMethodExit(onThrowable = Exception.class)
  private static void onExit(
      @Advice.Origin("#m") String methodName,
      @Advice.Thrown Throwable throwable,
      @Advice.FieldValue(value = "log") Logger log) {

    log.info("Exiting advised method: {}", methodName);

    if (throwable != null) {
      log.error("Exception thrown in advised method: {}", methodName, throwable);
      PlatformTransactionManager.rollback();
      return;
    }

    PlatformTransactionManager.commit();
  }
}
