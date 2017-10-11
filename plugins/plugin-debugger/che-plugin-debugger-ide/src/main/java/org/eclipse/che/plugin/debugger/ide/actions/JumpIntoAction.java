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
package org.eclipse.che.plugin.debugger.ide.actions;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action which allows step over in debugger session
 *
 * @author Mykola Morhun
 */
public class JumpIntoAction extends AbstractPerspectiveAction {

  private final DebuggerManager debuggerManager;
  private final AppContext appContext;
  private final EditorAgent editorAgent;

  @Inject
  public JumpIntoAction(
      DebuggerManager debuggerManager,
      AppContext appContext,
      EditorAgent editorAgent,
      DebuggerLocalizationConstant locale,
      DebuggerResources resources) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        "jump into",
        "jump into description",
        null,
        resources.stepOver());
    this.debuggerManager = debuggerManager;
    this.appContext = appContext;
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    EditorPartPresenter editor = editorAgent.getActiveEditor();
    int line = ((TextEditor) editor).getCursorPosition().getLine();
    String path = editor.getEditorInput().getFile().getLocation().toString();
    if (debugger != null) {
      debugger.jumpInto(line, path);
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    event.getPresentation().setEnabled(debugger != null && debugger.isSuspended());
  }
}
