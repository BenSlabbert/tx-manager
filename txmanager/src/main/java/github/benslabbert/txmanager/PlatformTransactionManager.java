/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static access to the underlying implementation of {@link TransactionManager}<br>
 * This is mainly used by the provided ByteBuddy Agent to apply advice, however, client code can use
 * it as well.
 */
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

  public static void beforeCommit(Runnable runnable) {
    log.debug("add before commit task");
    transactionManager.beforeCommit(runnable);
  }

  public static void afterCommit(Runnable runnable) {
    log.debug("add after commit task");
    transactionManager.afterCommit(runnable);
  }

  public static void close() throws Exception {
    if (null == transactionManager) {
      return;
    }

    try {
      transactionManager.close();
    } finally {
      transactionManager = null;
    }
  }
}
