/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.annotation.Transactional;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.Origin;
import org.slf4j.Logger;

class RequiresExistingAdvice {

  private RequiresExistingAdvice() {}

  @OnMethodEnter
  static void onEnter(
      @Origin("#m") String methodName,
      @TransactionPropagation Transactional.Propagation propagation,
      @FieldValue(value = "log") Logger log) {

    if (log.isDebugEnabled()) {
      log.debug("Entering advised method: {} with propagation: {}", methodName, propagation);
    }

    PlatformTransactionManager.ensureActive();

    log.debug("transaction active method: {}", methodName);
  }
}
