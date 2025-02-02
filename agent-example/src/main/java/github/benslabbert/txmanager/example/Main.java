/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.example;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.TransactionManager;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    PlatformTransactionManager.setTransactionManager(getTransactionManager());

    Example example = new Example();
    example.requiresNew1();
    example.requiresNew2();

    try {
      example.requiresExisting();
    } catch (IllegalStateException e) {
      log.error("exception while calling example.requiresExisting: ", e);
    }

    example.commitWithExpectedException();

    try {
      example.rollBackForUnplannedException();
    } catch (IllegalStateException e) {
      log.error("exception while calling example.rollBackForUnplannedException: ", e);
    }

    try {
      example.nestingWithException();
    } catch (IllegalStateException e) {
      log.error("exception while calling example.nestingWithException: ", e);
    }

    PlatformTransactionManager.close();
  }

  private static TransactionManager getTransactionManager() {
    return new TransactionManager() {
      private int txCount = 0;

      @Override
      public Connection getConnection() {
        if (txCount < 1) {
          throw new IllegalStateException("getConnection: no active transaction");
        }
        return null;
      }

      @Override
      public void begin() {
        txCount++;
        log.info("begin transaction: {}", txCount);
      }

      @Override
      public void ensureActive() {
        if (txCount < 1) {
          throw new IllegalStateException("ensureActive: no active transaction");
        }
        log.info("ensure active transaction: {}", txCount);
      }

      @Override
      public void commit() {
        if (txCount < 1) {
          throw new IllegalStateException("commit: no active transaction");
        }
        txCount--;
        log.info("commit transaction: {}", txCount);
      }

      @Override
      public void beforeCommit(Runnable runnable) {
        if (txCount < 1) {
          throw new IllegalStateException("beforeCommit: no active transaction");
        }
        log.info("beforeCommit");
        runnable.run();
      }

      @Override
      public void afterCommit(Runnable runnable) {
        if (txCount != 1) {
          throw new IllegalStateException("afterCommit: active transaction");
        }
        log.info("afterCommit");
        runnable.run();
      }

      @Override
      public void rollback() {
        if (txCount < 1) {
          throw new IllegalStateException("rollback: no active transaction");
        }
        txCount--;
        log.info("rollback transaction: {}", txCount);
      }

      @Override
      public void close() {
        txCount = 0;
        log.info("close TransactionManager: {}", txCount);
      }
    };
  }
}
