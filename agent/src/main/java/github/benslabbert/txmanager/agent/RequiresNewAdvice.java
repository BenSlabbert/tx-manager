/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.agent;

import static java.util.stream.Collectors.toSet;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.annotation.Transactional.Propagation;
import java.util.Arrays;
import java.util.Set;
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
      @Origin("#t\\##m") String methodName,
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
      @Origin("#t\\##m") String methodName,
      @TransactionIgnore String ignore,
      @TransactionDoNotRollBackFor String doNotRollBackFor,
      @TransactionPropagation Propagation propagation,
      @Thrown(readOnly = false) Throwable throwable) {

    if (log.isDebugEnabled()) {
      log.debug(
          "Exiting method: {} with propagation: {} with error ? {}",
          methodName,
          propagation,
          null != throwable);
    }

    if (null == throwable) {
      log.debug("commit after method: {}", methodName);
      PlatformTransactionManager.commit();
      return;
    }

    log.error("Exception thrown in advised method: {}", methodName, throwable);

    String throwableCanonicalName = throwable.getClass().getCanonicalName();
    Set<String> doNoRollBackSet = Arrays.stream(doNotRollBackFor.split(",")).collect(toSet());
    Set<String> ignoreSet = Arrays.stream(ignore.split(",")).collect(toSet());

    if (ignoreSet.contains(throwableCanonicalName)) {
      // tmp var as the logging may be lazily evaluated and show null
      log.warn("Ignoring exception", throwable);
      // set this to null otherwise it will be thrown later
      throwable = null;
    }

    if (doNoRollBackSet.contains(throwableCanonicalName)) {
      log.error("Transaction will not rollback for exception {}", throwableCanonicalName);
      PlatformTransactionManager.commit();
    } else {
      PlatformTransactionManager.rollback();
    }
  }
}
