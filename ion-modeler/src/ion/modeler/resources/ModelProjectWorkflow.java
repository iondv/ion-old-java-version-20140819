package ion.modeler.resources;

import org.eclipse.core.resources.IProject;

public class ModelProjectWorkflow extends IonModelResource{
	public ModelProjectWorkflow(IProject parent) {
	  super(parent.getFolder("workflows"));
	  name = "workflows";
	  displayName = "Бизнес процессы";
	}
}
