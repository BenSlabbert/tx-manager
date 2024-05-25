/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.example;

import github.benslabbert.txmanager.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
public class TransactionalExample {

  private static final Logger log = LoggerFactory.getLogger(TransactionalExample.class);

  @Transactional(propagation = Transactional.Propagation.REQUIRES_NEW)
  public void requiresNew1() {
    System.out.println("requiresNew1");
  }

  @Transactional
  public void requiresNew2() {
    System.out.println("requiresNew2");
  }

  @Transactional(propagation = Transactional.Propagation.REQUIRES_EXISTING)
  public void requiresExisting() {
    System.out.println("requiresExisting");
  }
}
