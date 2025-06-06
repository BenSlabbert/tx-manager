/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.agent;

import static github.benslabbert.txmanager.annotation.AlreadyTransformed.Transformer.AGENT;

import github.benslabbert.txmanager.agent.delegate.AfterCommitDelegator;
import github.benslabbert.txmanager.agent.delegate.BeforeCommitDelegator;
import github.benslabbert.txmanager.annotation.AfterCommit;
import github.benslabbert.txmanager.annotation.AlreadyTransformed;
import github.benslabbert.txmanager.annotation.AlreadyTransformed.Transformer;
import github.benslabbert.txmanager.annotation.BeforeCommit;
import github.benslabbert.txmanager.annotation.Transactional;
import github.benslabbert.txmanager.annotation.Transactional.Propagation;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
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
  private static final Generic GENERIC_VOID = Generic.OfNonGenericType.ForLoadedType.of(void.class);
  private static final Generic GENERIC_RUNNABLE =
      Generic.OfNonGenericType.ForLoadedType.of(Runnable.class);

  private TxManagerAgent() {}

  public static void premain(String agentArgument, Instrumentation instrumentation) {
    log.info("premain agentArgument: {}", agentArgument);
    try {
      new AgentBuilder.Default()
          .with(AgentListener.create())
          .type(TxManagerAgent.matcher())
          .transform((builder, target, a, b, c) -> apply(builder, target, AGENT))
          .installOn(instrumentation);
    } catch (Exception e) {
      log.info(e.getMessage());
    }
  }

  public static ElementMatcher<TypeDescription> matcher() {
    return target ->
        target.getDeclaredMethods().stream()
            .filter(
                s ->
                    s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class)
                        || s.getDeclaredAnnotations().isAnnotationPresent(AfterCommit.class)
                        || s.getDeclaredAnnotations().isAnnotationPresent(BeforeCommit.class))
            .anyMatch(
                s -> !s.getDeclaredAnnotations().isAnnotationPresent(AlreadyTransformed.class));
  }

  private static boolean moreThanOneLoadablePresent(Loadable<?>... loadables) {
    int cnt = 0;
    for (Loadable<?> loadable : loadables) {
      if (null != loadable) cnt++;
      if (cnt > 1) return true;
    }

    return false;
  }

  public static Builder<?> apply(
      Builder<?> builder, TypeDescription target, Transformer transformer) {

    List<InDefinedShape> advisedMethods =
        target.getDeclaredMethods().stream()
            .filter(
                s ->
                    s.getDeclaredAnnotations().isAnnotationPresent(Transactional.class)
                        || s.getDeclaredAnnotations().isAnnotationPresent(AfterCommit.class)
                        || s.getDeclaredAnnotations().isAnnotationPresent(BeforeCommit.class))
            .filter(s -> !s.getDeclaredAnnotations().isAnnotationPresent(AlreadyTransformed.class))
            .toList();

    if (advisedMethods.isEmpty()) {
      return builder;
    }

    for (InDefinedShape tm : advisedMethods) {
      Loadable<Transactional> transactional =
          tm.getDeclaredAnnotations().ofType(Transactional.class);
      Loadable<BeforeCommit> beforeCommit = tm.getDeclaredAnnotations().ofType(BeforeCommit.class);
      Loadable<AfterCommit> afterCommit = tm.getDeclaredAnnotations().ofType(AfterCommit.class);

      if (moreThanOneLoadablePresent(transactional, beforeCommit, afterCommit)) {
        throw new AnnotationDeclarationException(transactional, beforeCommit, afterCommit);
      }

      builder =
          builder
              .method(md -> md.equals(tm))
              .intercept(SuperMethodCall.INSTANCE)
              .annotateMethod(alreadyTransformed(transformer))
              .method(ElementMatchers.isAbstract().and(ElementMatchers.isDeclaredBy(target)))
              .withoutCode()
              .annotateMethod(alreadyTransformed(transformer));

      if (null != transactional) {
        builder = handleTransactional(builder, tm, transactional.load());
        continue;
      }

      Generic returnType = tm.getReturnType();
      log.info(
          "returnType: {} {} {}",
          returnType,
          returnType.equals(GENERIC_VOID),
          returnType.equals(GENERIC_RUNNABLE));

      if (null != beforeCommit) {
        if (GENERIC_RUNNABLE.equals(returnType)) {
          builder = builder.visit(getBeforeCommitAdviceRunnable().on(md -> md.equals(tm)));
        } else if (GENERIC_VOID.equals(returnType)) {
          // MethodDelegation creates a new method implementation that doesn't preserve the
          // annotations from the original method. This includes the AlreadyTransformed annotation
          // applied earlier
          MethodDelegation methodDelegation = MethodDelegation.to(BeforeCommitDelegator.class);
          builder =
              builder
                  .method(md -> md.equals(tm))
                  .intercept(methodDelegation)
                  .annotateMethod(alreadyTransformed(transformer));
        } else {
          log.error("beforeCommit: unable to advise {} return method {}d", returnType, tm);
        }
      }

      if (null != afterCommit) {
        if (GENERIC_RUNNABLE.equals(returnType)) {
          builder = builder.visit(getAfterCommitAdviceRunnable().on(md -> md.equals(tm)));
        } else if (GENERIC_VOID.equals(returnType)) {
          // MethodDelegation creates a new method implementation that doesn't preserve the
          // annotations from the original method. This includes the AlreadyTransformed annotation
          // applied earlier
          MethodDelegation methodDelegation = MethodDelegation.to(AfterCommitDelegator.class);
          builder =
              builder
                  .method(md -> md.equals(tm))
                  .intercept(methodDelegation)
                  .annotateMethod(alreadyTransformed(transformer));
        } else {
          log.error("afterCommit: unable to advise {} return method {}d", returnType, tm);
        }
      }
    }

    return builder;
  }

  private static AnnotationDescription alreadyTransformed(Transformer transformer) {
    return AnnotationDescription.Builder.ofType(AlreadyTransformed.class)
        .define("transformedBy", transformer)
        .build();
  }

  private static Builder<?> handleTransactional(
      Builder<?> builder, InDefinedShape tm, Transactional transactional) {
    String doNotRollBackFor =
        Arrays.stream(transactional.doNotRollBackFor())
            .distinct()
            .map(Class::getCanonicalName)
            .collect(Collectors.joining(","));
    String ignore =
        Arrays.stream(transactional.ignore())
            .distinct()
            .map(Class::getCanonicalName)
            .collect(Collectors.joining(","));
    Propagation propagation = transactional.propagation();

    // todo:
    //  for agent mode we can get this from agentArgument
    //  for plugin mode we can get this from System.getProperty
    String type = System.getProperty("txmanager.plugin.advice.type", "decorator");
    boolean decoratorImpl = !"implementation".equalsIgnoreCase(type);
    log.info("type: {}", type);
    log.info("decoratorImpl: {}", decoratorImpl);

    // there are two options we can apply advice as an implementation or as a decorator:
    // https://github.com/raphw/byte-buddy/issues/893
    // decorator: this seems to not modify the code, but injects the advice around the existing
    // implementation
    if (decoratorImpl) {
      switch (propagation) {
        case REQUIRES_NEW ->
            builder =
                builder.visit(
                    getRequiresNewAdvice(propagation, doNotRollBackFor, ignore)
                        .on(md -> md.equals(tm)));
        case REQUIRES_EXISTING ->
            builder = builder.visit(getRequiresExistingAdvice(propagation).on(md -> md.equals(tm)));
        case SAVE_POINT -> log.error("unsupported propagation: {}", propagation);
      }
    } else {
      // implementation: this changes the existing code and hides the original implementation with
      // some funny methods like: commitWithExpectedException$original$mbGYujRw();
      switch (propagation) {
        case REQUIRES_NEW ->
            builder =
                builder
                    .method(md -> md.equals(tm))
                    .intercept(getRequiresNewAdvice(propagation, doNotRollBackFor, ignore));
        case REQUIRES_EXISTING ->
            builder =
                builder
                    .method(md -> md.equals(tm))
                    .intercept(getRequiresExistingAdvice(propagation));
        case SAVE_POINT -> log.error("unsupported propagation: {}", propagation);
      }
    }

    return builder;
  }

  private static Advice getBeforeCommitAdviceRunnable() {
    return Advice.withCustomMapping().to(BeforeCommitAdviceRunnable.class);
  }

  private static Advice getAfterCommitAdviceRunnable() {
    return Advice.withCustomMapping().to(AfterCommitAdviceRunnable.class);
  }

  private static Advice getRequiresExistingAdvice(Propagation propagation) {
    return Advice.withCustomMapping()
        .bind(TransactionPropagation.class, propagation)
        .to(RequiresExistingAdvice.class);
  }

  private static Advice getRequiresNewAdvice(
      Propagation propagation, String doNotRollBackFor, String ignore) {
    return Advice.withCustomMapping()
        .bind(TransactionPropagation.class, propagation)
        .bind(TransactionDoNotRollBackFor.class, doNotRollBackFor)
        .bind(TransactionIgnore.class, ignore)
        .to(RequiresNewAdvice.class);
  }
}
