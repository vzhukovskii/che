/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import com.google.inject.assistedinject.Assisted;

/**
 * Helps to create {@link OpenShiftProject} instances.
 *
 * @author Anton Korneta
 */
public interface OpenShiftProjectFactory {

  /** Creates {@link OpenShiftProject} instance by given name and workspace id. */
  OpenShiftProject create(
      @Assisted("name") String name, @Assisted("workspaceId") String workspaceId);

  /** Creates {@link OpenShiftProject} instance by given workspace id. */
  OpenShiftProject create(String workspaceId);
}
