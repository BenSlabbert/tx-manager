/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.example;

import github.benslabbert.txmanager.AsyncExecutor;
import github.benslabbert.txmanager.AsyncExecutorRegistry;
import github.benslabbert.txmanager.annotation.Async;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncExample {

  private static final Logger log = LoggerFactory.getLogger(AsyncExample.class);

  static {
    AsyncExecutorRegistry.addExecutor(
        new AsyncExecutor() {
          @Override
          public String name() {
            return "virtual";
          }

          @Override
          public ExecutorService executorService() {
            return Executors.newVirtualThreadPerTaskExecutor();
          }
        });

    AsyncExecutorRegistry.addExecutor(
        new AsyncExecutor() {
          @Override
          public String name() {
            return "single";
          }

          @Override
          public ExecutorService executorService() {
            return Executors.newSingleThreadExecutor();
          }
        });
  }

  @Async
  public void run() {
    log.info("run");
  }

  @Async
  public Runnable runnable() {
    return () -> log.info("runnable");
  }

  public void r() {}

  @Async
  public Future<String> future() {
      return CompletableFuture.completedFuture("");
  }
}
