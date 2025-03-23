/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutorService;

/** Indicates that the method should be run in an async manner. <br> */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

  /** Specify the name of the {@link ExecutorService} to use. */
  String value() default "";
}
