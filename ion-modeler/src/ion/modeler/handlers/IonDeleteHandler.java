package ion.modeler.handlers;

import java.io.IOException;
import java.util.Iterator;

import ion.modeler.Composer;
import ion.modeler.resources.IonEntityResource;
import ion.modeler.resources.IonModelResource;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonSectionResource;
import ion.modeler.resources.IonUserTypeResource;
import ion.modeler.resources.IonValidatorResource;
import ion.modeler.resources.IonViewResource;
import ion.modeler.resources.IonWorkflowResource;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;


public class IonDeleteHandler extends AbstractHandler {

	private void deleteObject(Object d) throws CoreException, IOException{
		Composer c = new Composer(((IonModelResource) d).Source().getProject());
		if (d instanceof IonEntityResource)
			c.deleteEntity(((IonEntityResource) d));
		
		if (d instanceof IonSectionResource)
			c.deleteSection((IonSectionResource)d);
		
		if (d instanceof IonNodeResource)
			c.deleteNode((IonNodeResource)d);

		if (d instanceof IonViewResource)
			c.deleteView((IonViewResource)d);
		
		if (d instanceof IonUserTypeResource)
			c.deleteUserType((IonUserTypeResource)d);
		
		if (d instanceof IonValidatorResource)
			c.deleteValidator((IonValidatorResource)d);
		
		if (d instanceof IonWorkflowResource)
			c.deleteWorkflow((IonWorkflowResource)d);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		Object d;
		try {
			Iterator<?> i = selection.iterator();
			while (i.hasNext()){
				d = i.next();
				deleteObject(d);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
