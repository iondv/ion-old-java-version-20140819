package ion.modeler.actions;

import ion.modeler.editors.EntityEditor;
import ion.modeler.editors.FormViewEditor;
import ion.modeler.editors.ListViewEditor;
import ion.modeler.editors.NodeEditor;
import ion.modeler.editors.PropertyTplEditor;
import ion.modeler.editors.SectionEditor;
import ion.modeler.editors.UserTypeEditor;
import ion.modeler.editors.ValidatorEditor;
import ion.modeler.editors.WorkflowEditor;
import ion.modeler.resources.IonEntityResource;
import ion.modeler.resources.IonFileBasedResource;
import ion.modeler.resources.IonFormViewResource;
import ion.modeler.resources.IonListViewResource;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonPropertyTemplateResource;
import ion.modeler.resources.IonSectionResource;
import ion.modeler.resources.IonUserTypeResource;
import ion.modeler.resources.IonValidatorResource;
import ion.modeler.resources.IonWorkflowResource;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class IonOpenAction extends Action {

	public IonOpenAction() {
		
	}

	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    IWorkbenchPage page = window.getActivePage();
		IStructuredSelection selection = (IStructuredSelection)window.getSelectionService().getSelection();
		Object d;
		try {
			Iterator<?> i = selection.iterator();
			while (i.hasNext()){
				d = i.next();
				if (d instanceof IonFileBasedResource){
					FileEditorInput input = new FileEditorInput((IFile)((IonFileBasedResource) d).Source());
					if (d instanceof IonEntityResource)
						page.openEditor(input, EntityEditor.ID, true);
					if (d instanceof IonSectionResource)
						page.openEditor(input, SectionEditor.ID, true);
					if (d instanceof IonNodeResource)
						page.openEditor(input, NodeEditor.ID, true);
					if (d instanceof IonListViewResource)
						page.openEditor(input, ListViewEditor.ID, true);
					if (d instanceof IonFormViewResource)
						page.openEditor(input, FormViewEditor.ID, true);
					if (d instanceof IonUserTypeResource)
						page.openEditor(input, UserTypeEditor.ID, true);
					if (d instanceof IonValidatorResource)
						page.openEditor(input, ValidatorEditor.ID, true);
					if (d instanceof IonWorkflowResource)
						page.openEditor(input, WorkflowEditor.ID, true);
					if (d instanceof IonPropertyTemplateResource)
						page.openEditor(input, PropertyTplEditor.ID, true);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}	
}
