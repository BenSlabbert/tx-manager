/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

  Propagation propagation() default Propagation.REQUIRES_NEW;

  Class<?>[] doNotRollBackFor() default {};

  enum Propagation {
    /** creates a new transaction is none exists */
    REQUIRES_NEW,

    /** requires an existing transaction to be open, throws if none exists */
    REQUIRES_EXISTING
  }
}
