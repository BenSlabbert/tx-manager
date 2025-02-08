/* Licensed under Apache-2.0 2025. */
package github.benslabbert.txmanager.agent;

import github.benslabbert.txmanager.annotation.AfterCommit;
import github.benslabbert.txmanager.annotation.BeforeCommit;
import github.benslabbert.txmanager.annotation.Transactional;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;

class AnnotationDeclarationException extends RuntimeException {

  AnnotationDeclarationException(
      Loadable<Transactional> transactional,
      Loadable<BeforeCommit> beforeCommit,
      Loadable<AfterCommit> afterCommit) {
    super(
        "cannot have more than one annotations: transactional="
            + transactional
            + ", beforeCommit="
            + beforeCommit
            + ", afterCommit="
            + afterCommit);
  }
}
