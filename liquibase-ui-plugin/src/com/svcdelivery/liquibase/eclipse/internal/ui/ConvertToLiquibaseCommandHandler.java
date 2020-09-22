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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Converts the selected projects to Liquibase projects.
 * 
 * @author nick
 * 
 */
public class ConvertToLiquibaseCommandHandler implements IHandler {

	public void addHandlerListener(final IHandlerListener handlerListener) {
	}

	public void dispose() {
	}

	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			StructuredSelection structured = (StructuredSelection) selection;
			for (final Object next : structured.toList()) {
				if (next instanceof IProject) {
					final IProject project = (IProject) next;
					try {
						LiquibaseNature.addNature(project);
						LiquibaseNature.addBuilder(project);
					} catch (final CoreException e) {
						throw new ExecutionException(e.getMessage());
					}
				}
			}
		}
		return null;
	}

	public final boolean isEnabled() {
		return true;
	}

	public final boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(final IHandlerListener handlerListener) {
	}

}
