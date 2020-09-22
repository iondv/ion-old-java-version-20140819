package ion.modeler.resources;

import org.eclipse.core.resources.IProject;

public class ModelProjectAttrTemplates extends IonModelResource {
	public ModelProjectAttrTemplates(IProject parent) {
		super(parent.getFolder("attrtpl"));
		name = "attrTpl";
		displayName = "Шаблоны атрибутов";
    }	
}
