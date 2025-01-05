/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.agent;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.annotation.Transactional.Propagation;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice.Thrown;
import org.slf4j.Logger;

final class RequiresNewAdvice {

  private RequiresNewAdvice() {}

  @OnMethodEnter
  static void onEnter(
      @FieldValue(value = "log") Logger log,
      @Origin("#m") String methodName,
      @TransactionPropagation Propagation propagation) {
    // Use the properties here
    if (log.isDebugEnabled()) {
      log.debug(
          "Starting new transaction with propagation: {} for method: {}", propagation, methodName);
    }

    PlatformTransactionManager.begin();
  }

  // todo: perhaps use Advice.withExceptionHandler to give custom exception handler
  @OnMethodExit(onThrowable = Exception.class)
  static void onExit(
      @FieldValue(value = "log") Logger log,
      @Origin("#m") String methodName,
      @TransactionDoNotRollBackFor String classes,
      @TransactionPropagation Propagation propagation,
      @Thrown(readOnly = false) Throwable throwable) {

    if (log.isDebugEnabled()) {
      log.debug(
          "Exiting method: {} with propagation: {} with error ? {}",
          methodName,
          propagation,
          null == throwable);
    }

    if (null == throwable) {
      log.debug("commit after method: {}", methodName);
      PlatformTransactionManager.commit();
      return;
    }

    log.error("Exception thrown in advised method: {}", methodName, throwable);
    for (var clazz : classes.split(",")) {
      if (clazz.equals(throwable.getClass().getCanonicalName())) {
        log.error(
            "Transaction will not rollback as thrown exception {} matches: {}",
            throwable.getClass().getCanonicalName(),
            clazz);
        PlatformTransactionManager.commit();
        // set this to null otherwise it will be thrown later
        throwable = null;
        return;
      }
    }
    PlatformTransactionManager.rollback();
  }
}
