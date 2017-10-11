/*
 * ******************************************************************************
 *  * Copyright (c) 2012-2017 Red Hat, Inc.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  * Contributors:
 *  *   Red Hat, Inc. - initial API and implementation
 *   ******************************************************************************
 */
package org.eclipse.che.api.debug.shared.dto.action;

import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.model.action.JumpIntoAction;
import org.eclipse.che.dto.shared.DTO;

/** @author Igor Vinokur */
@DTO
public interface JumpIntoActionDto extends ActionDto, JumpIntoAction {
  TYPE getType();

  void setType(TYPE type);

  JumpIntoActionDto withType(TYPE type);

  LocationDto getLocation();

  void setLocation(LocationDto location);

  JumpIntoActionDto withLocation(LocationDto location);
}
