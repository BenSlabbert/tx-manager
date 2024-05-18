/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager;

public interface TransactionManager extends AutoCloseable {

  void begin();

  void ensureActive();

  void commit();

  void rollback();
}
