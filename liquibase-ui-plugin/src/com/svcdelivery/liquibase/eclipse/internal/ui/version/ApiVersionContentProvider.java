package com.svcdelivery.liquibase.eclipse.internal.ui.version;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author nick
 */
public class ApiVersionContentProvider implements IStructuredContentProvider {

	/**
	 * TODO These should be contributed by plugins.
	 */
	private String[] versions = { "2.0", "3.0" };

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		return versions;
	}

}
