/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The agent adds this annotation to indicate that a method is already transformed.<br>
 * This is required as we can use the transformer plugin or the agent to enhance classes.<br>
 * Client code should not be using this annotation directly.
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AlreadyTransformed {

  Transformer transformedBy();

  enum Transformer {
    PLUGIN,
    AGENT
  }
}
