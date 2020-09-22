package ion.modeler.resources;

import org.eclipse.core.resources.IProject;

public class ModelProjectViews extends IonModelResource {
	
	public IonViewsResource views;

	public ModelProjectViews(IProject parent) {
		super(parent.getFolder("views"));
		name = "views";
		displayName = "Интерфейс";
		views = new IonViewsResource(parent.getFolder("views"), this);
	}
}
