package com.svcdelivery.liquibase.eclipse.ui.preferences;

import java.net.URL;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author nick
 * 
 */
public class URLLabelProvider implements ITableLabelProvider {

	public void addListener(final ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	public void removeListener(final ILabelProviderListener listener) {
	}

	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	public String getColumnText(final Object element, final int columnIndex) {
		String label = "";
		if (element instanceof URL) {
			URL url = (URL) element;
			label = url.toString();
		}
		return label;
	}

}
