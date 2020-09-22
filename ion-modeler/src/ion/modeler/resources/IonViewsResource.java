package ion.modeler.resources;

import org.eclipse.core.resources.IResource;

public class IonViewsResource extends IonModelResource {
	
	IonModelResource parent;
	
	public IonViewsResource(IResource src, IonModelResource parent) {
		super(src);
		this.parent = parent;
		if(parent instanceof IonNodeResource){
			name = this.parent.getName()+".views";
			displayName = "Представления";
		} else if(parent instanceof ModelProjectViews) {
			name = this.source.getName()+".global.views";
			displayName = "Глобальные представления";
		}
	}
	
	public IonModelResource Parent(){
		return parent;
	}

}
