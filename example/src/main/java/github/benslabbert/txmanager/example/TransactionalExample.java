/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.example;

import static github.benslabbert.txmanager.annotation.Transactional.Propagation.REQUIRES_EXISTING;
import static github.benslabbert.txmanager.annotation.Transactional.Propagation.REQUIRES_NEW;

import github.benslabbert.txmanager.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
class TransactionalExample {

  private static final Logger log = LoggerFactory.getLogger(TransactionalExample.class);

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
}
