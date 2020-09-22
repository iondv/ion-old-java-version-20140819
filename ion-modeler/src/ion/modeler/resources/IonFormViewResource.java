package ion.modeler.resources;

import org.eclipse.core.resources.IFile;

public class IonFormViewResource extends IonViewResource {

	public IonFormViewResource(IFile src, IonViewsResource node, IonEntityResource entity) {
		super(src,node,entity,src.getName().equals("create.json")?"создание":src.getName().equals("detail.json")?"детализация":"изменение");
	}

}
