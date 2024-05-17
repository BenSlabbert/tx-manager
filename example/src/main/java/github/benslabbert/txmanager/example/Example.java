/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.example;

import static org.slf4j.LoggerFactory.*;

import github.benslabbert.txmanager.annotation.Transactional;
import org.slf4j.Logger;

@Transactional
public class Example {

  private static final Logger log = getLogger(Example.class);

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
