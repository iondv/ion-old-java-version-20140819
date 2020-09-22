package ion.modeler.resources;

import org.eclipse.core.resources.IProject;

public class IonListViewValidators extends IonModelResource {

	public IonListViewValidators(IProject parent) {
		super(parent.getFolder("validators"));
		name = "validators";
		displayName = "Валидаторы";
	}

}
