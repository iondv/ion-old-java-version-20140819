package ion.modeler.resources;

import org.eclipse.core.resources.IFile;

public class IonListViewResource extends IonViewResource {

	public IonListViewResource(IFile src, IonViewsResource node, IonEntityResource entity){
		super(src,node,entity,"список");
	}

}
