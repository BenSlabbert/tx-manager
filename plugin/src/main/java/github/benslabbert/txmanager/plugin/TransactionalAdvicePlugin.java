import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;

import java.lang.annotation.Annotation;

public class TransactionalAdvicePlugin implements Plugin {

  @Override
  public DynamicType.Builder<?> apply(
      DynamicType.Builder<?> builder,
      TypeDescription typeDescription,
      ClassFileLocator classFileLocator) {

    return builder
        .method(
            isAnnotatedWith(named(Transactional.class.getCanonicalName()))
                .and(
                    declaresAnnotation(
                        annotationDescription -> {
                          Transactional load =
                              annotationDescription.prepare(Transactional.class).load();
                          return load.propagation() == Transactional.Propagation.REQUIRES_NEW;
                        })))
        .intercept(Advice.withCustomMapping()
            .bind(Transactional.class, new AnnotationDescription.Loadable<Transactional>() {
                @Override
                public Class<? extends Annotation> getAnnotationType() {
                    return Transactional.class;
                }

                @Override
                public Transactional load() {
                    return annotationDescription.prepare(Transactional.class).load();
                }
            }).to(RequiresNewAdvice.class))
        .method(
            isAnnotatedWith(named(Transactional.class.getCanonicalName()))
                .and(
                    declaresAnnotation(
                        annotationDescription -> {
                          Transactional load =
                              annotationDescription.prepare(Transactional.class).load();
                          return load.propagation() == Transactional.Propagation.REQUIRES_EXISTING;
                        })))
        .intercept(Advice.to(RequiresExistingAdvice.class));
  }

  @Override
  public void close() {
    // Implement any necessary cleanup here
  }
}
