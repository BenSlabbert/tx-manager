/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The following method should return {@link java.lang.Runnable} or void which will be executed
 * before the current transaction commits.
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeCommit {}
