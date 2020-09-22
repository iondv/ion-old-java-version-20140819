package ion.modeler.resources;

import ion.framework.meta.plain.StoredClassMeta;

import org.eclipse.core.resources.IFile;

public class IonEntityResource extends IonFileBasedResource {
	
	private String ancestor;

	public IonEntityResource(IFile src, StoredClassMeta meta) {
		super(src);
		refresh(meta);
	}
	
	public void refresh(StoredClassMeta meta){
		name = meta.name;
		displayName = meta.caption;
		ancestor = meta.ancestor;		
	}
	
	public String getAncestor(){
		return ancestor;
	}
}
