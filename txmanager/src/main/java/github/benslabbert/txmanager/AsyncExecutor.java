/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager;

import java.util.concurrent.ExecutorService;

public interface AsyncExecutor {

  /** must be globally unique in the application */
  String name();

  ExecutorService executorService();
}
