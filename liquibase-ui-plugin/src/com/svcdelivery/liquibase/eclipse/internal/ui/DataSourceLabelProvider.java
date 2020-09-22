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

import java.text.SimpleDateFormat;

import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.svcdelivery.liquibase.eclipse.api.ChangeSetItem;

/**
 * @author nick
 */
public class DataSourceLabelProvider implements ITableLabelProvider {

	/**
	 * Date formatter.
	 */
	private SimpleDateFormat sdf;

	/**
	 * Constructor.
	 */
	public DataSourceLabelProvider() {
		sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

	public void addListener(final ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	/**
	 * @param element
	 *            The element to check.
	 * @param property
	 *            The property to check.
	 * @return false.
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider
	 *      #isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public final boolean isLabelProperty(final Object element,
			final String property) {
		return false;
	}

	public void removeListener(final ILabelProviderListener listener) {

	}

	public final Image getColumnImage(final Object element,
			final int columnIndex) {
		Image image = null;
		if (columnIndex == 0) {
			if (element instanceof IConnectionProfile) {
				image = Activator.getImage("database.gif");
			} else if (element instanceof ChangeSetTreeItem) {
				image = Activator.getImage("script.gif");
			}
		}
		return image;
	}

	public final String getColumnText(final Object element,
			final int columnIndex) {
		String text = "";
		if (element instanceof IConnectionProfile) {
			final IConnectionProfile profile = (IConnectionProfile) element;
			if (columnIndex == 0) {
				text = profile.getName();
			}
		} else if (element instanceof ChangeSetTreeItem) {
			ChangeSetTreeItem item = (ChangeSetTreeItem) element;
			ChangeSetItem changeSet = item.getChangeSet();
			if (columnIndex == 0) {
				text = changeSet.getChangeLog();
			} else if (columnIndex == 1) {
				text = changeSet.getId();
			} else if (columnIndex == 2) {
				text = changeSet.getTag();
			} else if (columnIndex == 3) {
				text = sdf.format(changeSet.getDateExecuted());
			} else if (columnIndex == 4) {
				text = changeSet.getExecType();
			}
		}
		return text;
	}
}
