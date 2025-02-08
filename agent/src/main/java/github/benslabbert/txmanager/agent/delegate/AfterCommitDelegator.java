/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.agent.delegate;

import github.benslabbert.txmanager.PlatformTransactionManager;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.Logger;

public final class AfterCommitDelegator {

  private AfterCommitDelegator() {}

  //    @BeforeCommit
  //    public void beforeCommitVoid() {
  //        AfterCommitDelegator.wrap(new auxiliary.v1Kp9I6P(this));
  //    }
  // however the client code now depends on this class...
  public static void wrap(@SuperCall Runnable runnable, @FieldValue(value = "log") Logger log) {
    log.info("adding runnable to beforeCommit");
    PlatformTransactionManager.afterCommit(runnable);
  }
}
