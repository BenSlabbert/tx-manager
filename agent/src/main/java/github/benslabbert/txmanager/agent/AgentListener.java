/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.agent;

import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AgentListener implements Listener {

  private static final Logger log = LoggerFactory.getLogger(AgentListener.class);

  private AgentListener() {}

  static AgentListener create() {
    return new AgentListener();
  }

  @Override
  public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
    log.info("onDiscovery s {} b {}", s, b);
  }

  @Override
  public void onTransformation(
      TypeDescription target,
      ClassLoader classLoader,
      JavaModule javaModule,
      boolean b,
      DynamicType dynamicType) {
    log.info("onTransformation: {} b {}", target, b);
  }

  @Override
  public void onIgnored(
      TypeDescription target, ClassLoader classLoader, JavaModule javaModule, boolean b) {
    log.info("onIgnored: {} b {}", target, b);
  }

  @Override
  public void onError(
      String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
    log.info("onError b {}", b, throwable);
  }

  @Override
  public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
    log.info("onComplete s {} b {}", s, b);
  }
}
