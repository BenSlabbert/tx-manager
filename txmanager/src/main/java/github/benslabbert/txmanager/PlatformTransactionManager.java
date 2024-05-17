/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlatformTransactionManager {

  private PlatformTransactionManager() {}

  private static final Logger log = LoggerFactory.getLogger(PlatformTransactionManager.class);

  private static TransactionManager transactionManager = null;

  public static void setTransactionManager(TransactionManager instance) {
    if (null != transactionManager) {
      throw new IllegalStateException("TransactionManager already initialized");
    }
    transactionManager = instance;
  }

  public static void begin() {
    log.debug("begin transaction");
    transactionManager.begin();
  }

  public static void ensureActive() {
    log.debug("ensure active transaction");
    transactionManager.ensureActive();
  }

  public static void commit() {
    log.debug("commit transaction");
    transactionManager.commit();
  }

  public static void rollback() {
    log.debug("rollback transaction");
    transactionManager.rollback();
  }
}
