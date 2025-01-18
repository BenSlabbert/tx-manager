/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.example;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.txmanager.TransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExampleTest {

  @Mock private TransactionManager tm;

  private Example example;

  @BeforeEach
  void init() {
    PlatformTransactionManager.setTransactionManager(tm);
    example = new Example();
  }

  @AfterEach
  void cleanup() throws Exception {
    PlatformTransactionManager.close();
  }

  @Test
  void simple() {
    example.simple();

    verify(tm).begin();
    verify(tm).commit();
  }

  @Test
  void requiresNew1() {
    example.requiresNew1();

    verify(tm, times(2)).begin();
    verify(tm, times(2)).commit();
  }

  @Test
  void requiresNew2() {
    example.requiresNew2();

    verify(tm).begin();
    verify(tm).ensureActive();
    verify(tm).commit();
  }

  @Test
  void commitWithExpectedException() {
    assertThrows(IllegalArgumentException.class, example::commitWithExpectedException);

    verify(tm).begin();
    verify(tm).commit();
  }

  @Test
  void rollBackForUnplannedException() {
    assertThrows(IllegalStateException.class, example::rollBackForUnplannedException);

    verify(tm).begin();
    verify(tm).rollback();
  }

  @Test
  void ignoreNoCommit() {
    example.ignoreNoCommit();

    verify(tm).begin();
    verify(tm).rollback();
  }

  @Test
  void ignoreWithCommit() {
    example.ignoreWithCommit();

    verify(tm).begin();
    verify(tm).commit();
  }

  @Test
  void ignoreThrows() {
    assertThrows(IllegalArgumentException.class, example::ignoreThrows);

    verify(tm).begin();
    verify(tm).rollback();
  }

  @Test
  void requiresExisting() {
    example.requiresExisting();

    verify(tm).ensureActive();
  }
}
