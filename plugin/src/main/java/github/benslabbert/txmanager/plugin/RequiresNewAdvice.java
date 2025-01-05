/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.annotation.Transactional;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;

class RequiresNewAdvice {

  @Advice.OnMethodEnter
  public static void onEnter(
      @Advice.FieldValue(value = "log") Logger log,
      @CustomAnnotation Transactional.Propagation propagation) {
    // Use the properties here
    log.info("Propagation: {}", propagation);
  }

  @Advice.OnMethodExit
  public static void onExit() {
    // Implement any necessary logic here
  }
}
