/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.plugin;

import static net.bytebuddy.matcher.ElementMatchers.declaresAnnotation;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

import github.benslabbert.txmanager.annotation.Transactional;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

class TransactionalAdvicePlugin implements Plugin {

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
        .intercept(
            Advice.withCustomMapping()
                .bind(
                    Transactional.class,
                    new AnnotationDescription.Loadable<Transactional>() {
                      @Override
                      public Class<Transactional> getAnnotationType() {
                        return Transactional.class;
                      }

                      @Override
                      public Transactional load() {
                        return annotationDescription.prepare(Transactional.class).load();
                      }

                      @Override
                      public boolean isDocumented() {
                        return true;
                      }

                      @Override
                      public boolean isInherited() {
                        return false;
                      }

                      @Override
                      public boolean isSupportedOn(java.lang.String s) {
                        return false;
                      }

                      @Override
                      public boolean isSupportedOn(java.lang.annotation.ElementType e) {
                        return false;
                      }
                    })
                .to(RequiresNewAdvice.class))
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
    // nothing open
  }

  @Override
  public boolean matches(TypeDescription typeDefinitions) {
    AnnotationList declaredAnnotations = typeDefinitions.getDeclaredAnnotations();
    return declaredAnnotations.isAnnotationPresent(Transactional.class);
    // Implement any necessary cleanup here
  }
}
