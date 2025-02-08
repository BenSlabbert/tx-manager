/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.agent;

import github.benslabbert.txmanager.PlatformTransactionManager;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice.Return;
import org.slf4j.Logger;

final class BeforeCommitAdviceRunnable {

  private BeforeCommitAdviceRunnable() {}

  @OnMethodExit
  static void onExit(
      @Origin("#t\\##m") String methodName,
      @Return Runnable runnable,
      @FieldValue(value = "log") Logger log) {

    if (log.isDebugEnabled()) {
      log.debug("Adding before commit runnable from method: {}", methodName);
    }

    PlatformTransactionManager.beforeCommit(runnable);
  }
}
