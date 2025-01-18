/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.agent;

import static github.benslabbert.txmanager.annotation.AlreadyTransformed.Transformer.AGENT;

import github.benslabbert.txmanager.annotation.AlreadyTransformed;
import github.benslabbert.txmanager.annotation.AlreadyTransformed.Transformer;
import github.benslabbert.txmanager.annotation.Transactional;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://www.javaadvent.com/2019/12/a-beginners-guide-to-java-agents.html <br>
 * https://github.com/oiraqi/microhooks/blob/ac1b181c274bc620b003ab0f18c3e704ab711567/builder/lib/src/main/java/io/microhooks/builder/Builder.java#L10
 */
public final class TxManagerAgent {

  private static final Logger log = LoggerFactory.getLogger(TxManagerAgent.class);

  private TxManagerAgent() {}

  public static void premain(String agentArgument, Instrumentation instrumentation) {
    log.info("premain agentArgument: {}", agentArgument);
    try {
      new AgentBuilder.Default()
          .with(AgentListener.create())
          .type(TxManagerAgent.matcher())
          .transform((builder, target, _, _, _) -> apply(builder, target, AGENT))
          .installOn(instrumentation);
    } catch (Exception e) {
      log.info(e.getMessage());
    }
  }

  public static ElementMatcher<TypeDescription> matcher() {
    return target ->
        target.getDeclaredMethods().stream()
            .filter(s -> s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class))
            .anyMatch(
                s -> !s.getDeclaredAnnotations().isAnnotationPresent(AlreadyTransformed.class));
  }

  public static Builder<?> apply(
      Builder<?> builder, TypeDescription target, Transformer transformer) {
    // todo:
    //  for agent mode we can get this from agentArgument
    //  for plugin mode we can get this from System.getProperty
    String type = System.getProperty("txmanager.plugin.advice.type", "decorator");
    boolean decoratorImpl = !"implementation".equalsIgnoreCase(type);
    log.info("type: {}", type);
    log.info("decoratorImpl: {}", decoratorImpl);

    List<MethodDescription.InDefinedShape> transactionalMethods =
        target.getDeclaredMethods().stream()
            .filter(s -> s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class))
            .filter(s -> !s.getDeclaredAnnotations().isAnnotationPresent(AlreadyTransformed.class))
            .toList();

    builder =
        builder
            .method(ElementMatchers.isDeclaredBy(target))
            .intercept(SuperMethodCall.INSTANCE)
            .annotateMethod(
                AnnotationDescription.Builder.ofType(AlreadyTransformed.class)
                    .define("transformedBy", transformer)
                    .build())
            .method(ElementMatchers.isAbstract().and(ElementMatchers.isDeclaredBy(target)))
            .withoutCode()
            .annotateMethod(
                AnnotationDescription.Builder.ofType(AlreadyTransformed.class)
                    .define("transformedBy", transformer)
                    .build());

    if (transactionalMethods.isEmpty()) {
      return builder;
    }

    for (MethodDescription.InDefinedShape tm : transactionalMethods) {
      Transactional transactional =
          Objects.requireNonNull(tm.getDeclaredAnnotations().ofType(Transactional.class)).load();

      String doNotRollBackFor =
          Arrays.stream(transactional.doNotRollBackFor())
              .map(Class::getCanonicalName)
              .collect(Collectors.joining(","));
      String ignore =
          Arrays.stream(transactional.ignore())
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
                          .bind(TransactionIgnore.class, ignore)
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
                              .bind(TransactionIgnore.class, ignore)
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
}
