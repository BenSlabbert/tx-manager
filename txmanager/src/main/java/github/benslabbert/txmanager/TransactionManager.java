/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager;

public interface TransactionManager {

  void begin();

  void commit();

  void rollback();
}
