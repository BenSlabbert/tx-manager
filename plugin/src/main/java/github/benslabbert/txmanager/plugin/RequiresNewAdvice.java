/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.annotation.Transactional;
import java.util.Arrays;
import net.bytebuddy.asm.Advice;

class RequiresNewAdvice {

  @Advice.OnMethodEnter
  public static void onEnter(@Transactional Transactional transactional) {
    // Use the properties here
    System.out.println("Propagation: " + transactional.propagation());
    System.out.println("doNotRollBackFor: " + Arrays.toString(transactional.doNotRollBackFor()));
  }

  @Advice.OnMethodExit
  public static void onExit() {
    // Implement any necessary logic here
  }
}
