package ion.modeler.resources;

import org.eclipse.core.resources.IProject;

public class ModelProjectMeta extends IonModelResource {
	
	public ModelProjectMeta(IProject parent) {
		super(parent.getFolder("meta"));
		name = "meta";
		displayName = "Модель";
    }	
}
