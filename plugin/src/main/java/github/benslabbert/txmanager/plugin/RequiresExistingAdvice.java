/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.PlatformTransactionManager;
import java.util.Arrays;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.Origin;
import org.slf4j.Logger;

class RequiresExistingAdvice {

  private RequiresExistingAdvice() {}

  @OnMethodEnter
  private static void onEnter(
      @AllArguments(nullIfEmpty = true) Object[] args,
      @Origin("#m") String methodName,
      @FieldValue(value = "log") Logger log) {

    if (log.isDebugEnabled()) {
      log.debug("Entering advised method: {}", methodName);
      log.debug("args: {}", args == null ? "null" : Arrays.toString(args));
    }

    PlatformTransactionManager.ensureActive();

    log.debug("transaction active method: {}", methodName);
  }
}
