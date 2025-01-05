/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import github.benslabbert.txmanager.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

/**
 * useful example for this guy is here:
 * https://github.com/oiraqi/microhooks/blob/ac1b181c274bc620b003ab0f18c3e704ab711567/builder/lib/src/main/java/io/microhooks/builder/Builder.java#L10
 */
public class TransactionalAdvicePlugin implements Plugin {

  private final boolean decoratorImpl;

  public TransactionalAdvicePlugin() {
    // decorator/implementation
    String type = System.getProperty("txmanager.plugin.advice.type", "decorator");
    decoratorImpl = !"implementation".equalsIgnoreCase(type);
    System.err.println("type: " + type);
    System.err.println("decoratorImpl: " + decoratorImpl);
  }

  @Override
  public boolean matches(TypeDescription target) {
    return target.getDeclaredMethods().stream()
        .anyMatch(s -> s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class));
  }

  @Override
  public DynamicType.Builder<?> apply(
      DynamicType.Builder<?> builder, TypeDescription target, ClassFileLocator classFileLocator) {

    List<InDefinedShape> transactionalMethods =
        target.getDeclaredMethods().stream()
            .filter(s -> s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class))
            .toList();

    if (transactionalMethods.isEmpty()) {
      return builder;
    }

    for (InDefinedShape tm : transactionalMethods) {
      Transactional transactional =
          Objects.requireNonNull(tm.getDeclaredAnnotations().ofType(Transactional.class)).load();

      String doNotRollBackFor =
          Arrays.stream(transactional.doNotRollBackFor())
              .map(Class::getCanonicalName)
              .collect(Collectors.joining(","));
      Transactional.Propagation propagation = transactional.propagation();

      // there are two options we can apply advice as an implementation or as a decorator:
      // https://github.com/raphw/byte-buddy/issues/893
      // decorator: this seems to not modify the code, but injects the advice around the existing
      // implementation
      if (decoratorImpl) {
        switch (propagation) {
          case REQUIRES_NEW ->
              builder =
                  builder.visit(
                      Advice.withCustomMapping()
                          .bind(TransactionPropagation.class, propagation)
                          .bind(TransactionDoNotRollBackFor.class, doNotRollBackFor)
                          .to(RequiresNewAdvice.class)
                          .on(md -> md.equals(tm)));

          case REQUIRES_EXISTING ->
              builder =
                  builder.visit(
                      Advice.withCustomMapping()
                          .bind(TransactionPropagation.class, propagation)
                          .to(RequiresExistingAdvice.class)
                          .on(md -> md.equals(tm)));
        }
      } else {
        // implementation: this changes the existing code and hides the original implementation with
        // some funny methods like: commitWithExpectedException$original$mbGYujRw();
        switch (propagation) {
          case REQUIRES_NEW ->
              builder =
                  builder
                      .method(md -> md.equals(tm))
                      .intercept(
                          Advice.withCustomMapping()
                              .bind(TransactionPropagation.class, propagation)
                              .bind(TransactionDoNotRollBackFor.class, doNotRollBackFor)
                              .to(RequiresNewAdvice.class));

          case REQUIRES_EXISTING ->
              builder =
                  builder
                      .method(md -> md.equals(tm))
                      .intercept(
                          Advice.withCustomMapping()
                              .bind(TransactionPropagation.class, propagation)
                              .to(RequiresExistingAdvice.class));
        }
      }
    }

    return builder;
  }

  @Override
  public void close() {
    // nothing open
  }
}
