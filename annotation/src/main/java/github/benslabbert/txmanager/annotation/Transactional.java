/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a transaction must be configured.<br>
 * This annotation is process by a java agent, in this case ByteBuddy.<br>
 * The annotation can be placed on any method with any scope (even private).
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

  /** Specify if the transaction should be reused, existing or a new one created. */
  Propagation propagation() default Propagation.REQUIRES_NEW;

  /**
   * Specify for which exceptions the current transaction should commit and not rollback for.<br>
   * The exception will still be thrown.
   */
  Class<? extends Exception>[] doNotRollBackFor() default {};

  /**
   * Specify for which exceptions the current transaction should ignore.<br>
   * These exceptions will not be thrown, but rather handled by the advice.<br>
   * Depending on {@link Transactional#doNotRollBackFor()} the transaction may or may not be
   * committed.
   */
  Class<? extends Exception>[] ignore() default {};

  enum Propagation {
    /** creates a new transaction is none exists */
    REQUIRES_NEW,

    /** requires an existing transaction to be open, throws if none exists */
    REQUIRES_EXISTING
  }
}
