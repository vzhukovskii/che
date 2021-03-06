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
package org.eclipse.che.api.system.shared.dto;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.system.shared.event.EventType;
import org.eclipse.che.api.system.shared.event.SystemEvent;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for {@link SystemEvent}.
 *
 * @author Yevhenii Voevodin
 */
@DTO
@EventOrigin("system")
public interface SystemEventDto extends SystemEvent {

  void setType(EventType type);

  SystemEventDto withType(EventType type);
}
