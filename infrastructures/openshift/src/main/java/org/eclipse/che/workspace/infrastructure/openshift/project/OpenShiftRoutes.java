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

import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject.CHE_WORKSPACE_LABEL;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Defines an internal API for managing {@link Route} instances in {@link OpenShiftRoutes#namespace
 * predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftRoutes {
  private final String namespace;
  private final String workspaceId;
  private final OpenShiftClientFactory clientFactory;

  OpenShiftRoutes(String namespace, String workspaceId, OpenShiftClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
  }

  /**
   * Creates specified route.
   *
   * @param route route to create
   * @return created route
   * @throws InfrastructureException when any exception occurs
   */
  public Route create(Route route) throws InfrastructureException {
    try (OpenShiftClient client = clientFactory.create()) {
      route.getMetadata().getLabels().put(CHE_WORKSPACE_LABEL, workspaceId);
      return client.routes().inNamespace(namespace).create(route);
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  /**
   * Returns all existing routes.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public List<Route> get() throws InfrastructureException {
    try (OpenShiftClient client = clientFactory.create()) {
      return client
          .routes()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_LABEL, workspaceId)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  /**
   * Deletes all existing routes.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public void delete() throws InfrastructureException {
    try (OpenShiftClient client = clientFactory.create()) {
      client.routes().inNamespace(namespace).withLabel(CHE_WORKSPACE_LABEL, workspaceId).delete();
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }
}
