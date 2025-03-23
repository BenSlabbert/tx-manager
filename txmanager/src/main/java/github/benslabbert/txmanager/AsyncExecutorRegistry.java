/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager;

import jakarta.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public final class AsyncExecutorRegistry {

  private static final Map<String, ExecutorService> EXECUTORS = new ConcurrentHashMap<>(2);

  private AsyncExecutorRegistry() {}

  public static void addExecutor(AsyncExecutor executor) {
    EXECUTORS.putIfAbsent(executor.name(), executor.executorService());
  }

  @Nullable public static ExecutorService getExecutor(String name) {
    return EXECUTORS.get(name);
  }
}
