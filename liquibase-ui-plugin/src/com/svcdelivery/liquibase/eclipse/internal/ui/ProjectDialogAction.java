package com.svcdelivery.liquibase.eclipse.internal.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class ProjectDialogAction implements IViewActionDelegate {

	public void run(IAction action) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		ProjectDialog dialog = new ProjectDialog(shell);
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void init(IViewPart view) {
	}

}
