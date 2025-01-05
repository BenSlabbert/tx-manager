/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.agent.TxManagerAgent;
import github.benslabbert.txmanager.annotation.AlreadyTransformed;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType.Builder;

public class TransactionalAdvicePlugin implements Plugin {

  @Override
  public boolean matches(TypeDescription target) {
    return TxManagerAgent.matcher().matches(target);
  }

  @Override
  public Builder<?> apply(Builder<?> builder, TypeDescription target, ClassFileLocator cfl) {
    return TxManagerAgent.apply(builder, target, AlreadyTransformed.Transformer.PLUGIN);
  }

  @Override
  public void close() {
    // nothing open
  }
}
