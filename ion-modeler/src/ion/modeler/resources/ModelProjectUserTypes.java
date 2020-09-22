package ion.modeler.resources;

import org.eclipse.core.resources.IProject;

public class ModelProjectUserTypes extends IonModelResource {
	public ModelProjectUserTypes(IProject parent) {
		super(parent.getFolder("meta/types"));
		name = "types";
		displayName = "Пользовательские типы";
	}
}
