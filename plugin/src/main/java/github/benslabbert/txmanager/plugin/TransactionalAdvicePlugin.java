/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

public class TransactionalAdvicePlugin implements Plugin {

  @Override
  public boolean matches(TypeDescription target) {
    return target.getDeclaredMethods().stream()
        .anyMatch(s -> s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class));
  }

  @Override
  public DynamicType.Builder<?> apply(
      DynamicType.Builder<?> builder, TypeDescription target, ClassFileLocator classFileLocator) {

    System.err.println("processing: " + target);
    List<MethodDescription.InDefinedShape> transactionalMethods =
        target.getDeclaredMethods().stream()
            .filter(s -> s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class))
            .toList();

    if (transactionalMethods.isEmpty()) {
      System.err.println("no transactional methods found");
      return builder;
    }

    for (MethodDescription.InDefinedShape tm : transactionalMethods) {
      System.err.println("processing: " + tm);
      Transactional transactional = tm.getDeclaredAnnotations().ofType(Transactional.class).load();
      Class<?>[] doNotRollBackFor = transactional.doNotRollBackFor();
      Transactional.Propagation propagation = transactional.propagation();
      System.err.println("@Transactional doNotRollBackFor: " + Arrays.toString(doNotRollBackFor));
      System.err.println("@Transactional propagation: " + propagation);

      builder =
          builder
              .method(md -> md.equals(tm))
              .intercept(
                  Advice.withCustomMapping()
                      .bind(CustomAnnotation.class, propagation)
                      .to(RequiresNewAdvice.class));
    }

    return builder;
  }

  @Override
  public void close() {
    // nothing open
  }
}
