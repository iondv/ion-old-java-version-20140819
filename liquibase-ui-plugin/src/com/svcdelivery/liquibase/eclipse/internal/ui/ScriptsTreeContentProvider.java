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

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author nick
 */
public class ScriptsTreeContentProvider implements ITreeContentProvider {

	/**
	 * The cache.
	 */
	private ChangeLogCache cache;

	public void dispose() {
	}

	public final void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
		if (newInput instanceof ChangeLogCache) {
			cache = (ChangeLogCache) newInput;
		}
	}

	public final Object[] getElements(final Object inputElement) {
		Object[] elements = new Object[0];
		if (cache != null) {
			Set<IFile> changeLogs = cache.getChangeLogs();
			if (changeLogs != null) {
				elements = changeLogs.toArray();
			}
		}
		return elements;
	}

	public final Object[] getChildren(final Object parentElement) {
		return null;
	}

	public final Object getParent(final Object element) {
		return null;
	}

	public final boolean hasChildren(final Object element) {
		return false;
	}

}
