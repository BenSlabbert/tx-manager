/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.agent;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.annotation.Transactional.Propagation;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.Origin;
import org.slf4j.Logger;

final class RequiresExistingAdvice {

  private RequiresExistingAdvice() {}

  @OnMethodEnter
  static void onEnter(
      @Origin("#t\\##m") String methodName,
      @TransactionPropagation Propagation propagation,
      @FieldValue(value = "log") Logger log) {

    if (log.isDebugEnabled()) {
      log.debug("Entering advised method: {} with propagation: {}", methodName, propagation);
    }

    PlatformTransactionManager.ensureActive();

    log.debug("transaction active method: {}", methodName);
  }
}
