/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@interface TransactionDoNotRollBackFor {}
