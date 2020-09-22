/**
 * Copyright 2012 Nick Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.svcdelivery.liquibase.eclipse.internal.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author nick
 */
public class RollbackCommandHandler extends Action implements IHandler {

	/**
	 * The current shell.
	 */
	private Shell shell;
	/**
	 * The current selection.
	 */
	private ISelection selection;

	/**
	 * Default constructor.
	 */
	public RollbackCommandHandler() {
	}

	/**
	 * @param currentShell
	 *            The current shell.
	 * @param currentSelection
	 *            The current selection.
	 */
	public RollbackCommandHandler(final Shell currentShell,
			final ISelection currentSelection) {
		shell = currentShell;
		selection = currentSelection;
		setText("Rollback Script");
	}

	public void addHandlerListener(final IHandlerListener handlerListener) {
	}

	public void dispose() {
	}

	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		selection = HandlerUtil.getCurrentSelection(event);
		shell = HandlerUtil.getActiveShell(event);
		IFile file = null;
		if (selection instanceof StructuredSelection) {
			final StructuredSelection structured = (StructuredSelection) selection;
			if (structured.size() == 1) {
				file = (IFile) structured.getFirstElement();
			}
		}
		IWizard targetWizard = new RollbackScriptsWizard(file);
		showWizard(targetWizard);
		return null;
	}

	@Override
	public final void run() {
		ChangeSetTreeItem item = null;
		if (selection instanceof StructuredSelection) {
			final StructuredSelection structured = (StructuredSelection) selection;
			if (structured.size() == 1)
				item = (ChangeSetTreeItem) structured.getFirstElement();
		}
		IWizard targetWizard = new RollbackChangeSetWizard(item);
		showWizard(targetWizard);
	}

	/**
	 * @param targetWizard
	 *            The wizard to show.
	 */
	private void showWizard(final IWizard targetWizard) {
		WizardDialog dialog = new WizardDialog(shell, targetWizard);
		dialog.setPageSize(600, 500);
		dialog.open();
	}

	@Override
	public final boolean isEnabled() {
		return true;
	}

	@Override
	public final boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(final IHandlerListener handlerListener) {
	}

}
