/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.example;

import static github.benslabbert.txmanager.annotation.Transactional.Propagation.REQUIRES_EXISTING;
import static github.benslabbert.txmanager.annotation.Transactional.Propagation.REQUIRES_NEW;

import github.benslabbert.txmanager.annotation.AfterCommit;
import github.benslabbert.txmanager.annotation.BeforeCommit;
import github.benslabbert.txmanager.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Example {

  private static final Logger log = LoggerFactory.getLogger(Example.class);

  @Transactional
  void simple() {
    log.info("simple");
  }

  @Transactional(ignore = IllegalStateException.class)
  void ignoreNoCommit() {
    log.info("ignoreNoCommit");
    throw new IllegalStateException("exceptedException");
  }

  @Transactional(
      ignore = IllegalStateException.class,
      doNotRollBackFor = IllegalStateException.class)
  void ignoreWithCommit() {
    log.info("ignoreWithCommit");
    throw new IllegalStateException("exceptedException");
  }

  @Transactional(
      ignore = IllegalStateException.class,
      doNotRollBackFor = IllegalStateException.class)
  void ignoreThrows() {
    log.info("ignoreThrows");
    throw new IllegalArgumentException("exceptedException");
  }

  @Transactional(propagation = REQUIRES_NEW)
  void requiresNew1() {
    log.info("requiresNew1");
    privateRequiresNew();
  }

  @Transactional
  void requiresNew2() {
    log.info("requiresNew2");
    privateRequiresExisting();
  }

  @Transactional(propagation = REQUIRES_NEW)
  private void privateRequiresNew() {
    log.info("privateRequiresNew");
  }

  @Transactional(propagation = REQUIRES_EXISTING)
  private void privateRequiresExisting() {
    log.info("privateRequiresExisting");
  }

  @Transactional(doNotRollBackFor = {RuntimeException.class, IllegalArgumentException.class})
  void commitWithExpectedException() {
    log.info("commitWithExpectedException");
    throw new IllegalArgumentException("exceptedException");
  }

  @Transactional(doNotRollBackFor = {RuntimeException.class, IllegalArgumentException.class})
  void rollBackForUnplannedException() {
    log.info("rollBackForUnplannedException");
    throw new IllegalStateException("exceptedException");
  }

  @Transactional(propagation = REQUIRES_EXISTING)
  void requiresExisting() {
    log.info("requiresExisting");
  }

  @Transactional
  void nestingWithException() {
    log.info("nestingWithException");
    privateRequiresNewThrows();
  }

  @Transactional(propagation = REQUIRES_NEW)
  private void privateRequiresNewThrows() {
    log.info("privateRequiresNewThrows");
    throw new IllegalStateException("planned");
  }

  @BeforeCommit
  public Runnable beforeCommit() {
    log.info("beforeCommit");
    return () -> {};
  }

  @AfterCommit
  public Runnable afterCommit() {
    log.info("afterCommit");
    return () -> {};
  }
}
