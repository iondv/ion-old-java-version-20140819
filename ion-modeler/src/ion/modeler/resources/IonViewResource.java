package ion.modeler.resources;

import org.eclipse.core.resources.IFile;

public abstract class IonViewResource extends IonFileBasedResource {

	IonViewsResource parent;
	
	String suffix;
	
	public IonViewResource(IFile src, IonViewsResource node, IonEntityResource entity, String suffix) {
		super(src);
		this.suffix = suffix;
		refresh(node,entity);
	}
	
	public void refresh(IonViewsResource node, IonEntityResource entity){
		parent = node;
		name = node.Parent().getName()+"."+entity.getName()+"."+Source().getName();
		displayName = entity.getDisplayName()+" ("+suffix+")";		
	}
	
	public IonViewsResource getParent(){
		return parent;
	}
}
