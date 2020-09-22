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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author nick
 */
public class LiquibaseResultsLabelProvider implements ITableLabelProvider {

	/**
	 * Date formatter.
	 */
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public void addListener(final ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public final boolean isLabelProperty(final Object element,
			final String property) {
		return false;
	}

	public void removeListener(final ILabelProviderListener listener) {
	}

	public final Image getColumnImage(final Object element,
			final int columnIndex) {
		Image image = null;
		if (columnIndex == 0 && element instanceof LiquibaseResult) {
			final LiquibaseResultStatus status = ((LiquibaseResult) element)
					.getStatus();
			if (status != null) {
				image = Activator.getImage(status.getFileName());
			}
		}
		return image;
	}

	public final String getColumnText(final Object element,
			final int columnIndex) {
		String text = "";
		if (element instanceof LiquibaseResult) {
			final LiquibaseResult result = (LiquibaseResult) element;
			if (columnIndex == 0) {
				text = df.format(new Date(result.getTimestamp()));
			} else if (columnIndex == 1) {
				text = result.getScript();
			}
		}
		return text;
	}

}
