/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.PlatformTransactionManager;
import java.util.Arrays;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;

public class RequiresExistingAdvice {

  private RequiresExistingAdvice() {}

  @Advice.OnMethodEnter
  private static void onEnter(
      @Advice.AllArguments(nullIfEmpty = true) Object[] args,
      @Advice.Origin("#m") String methodName,
      @Advice.FieldValue(value = "log") Logger log) {

    log.debug("Entering advised method: {}", methodName);
    log.debug("args: {}", args == null ? "null" : Arrays.toString(args));

    PlatformTransactionManager.ensureActive();
  }
}
