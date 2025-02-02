/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager;

import java.sql.Connection;

public interface TransactionManager extends AutoCloseable {

  Connection getConnection();

  void begin();

  void ensureActive();

  void commit();

  /**
   * Execute the {@link Runnable} before a transaction is committed.<br>
   * This can ben called multiple times with different {@link Runnable}(s) which will be executed in
   * no specified order.<br>
   * The existing underlying transaction will be available.
   */
  void beforeCommit(Runnable runnable);

  /**
   * Execute the {@link Runnable} after a transaction is committed.<br>
   * This can ben called multiple times with different {@link Runnable}(s).<br>
   * The previous transaction will not be available.<br>
   * Provided {@link Runnable}(s) will be scheduled only after a successful commit and can be run in
   * parallel.
   */
  void afterCommit(Runnable runnable);

  void rollback();
}
