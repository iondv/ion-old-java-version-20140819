package ion.modeler.resources;

import ion.framework.meta.plain.StoredUserTypeMeta;

import org.eclipse.core.resources.IFile;

public class IonUserTypeResource extends IonFileBasedResource {
	
	public IonUserTypeResource(IFile src, StoredUserTypeMeta meta) {
		super(src);
		refresh(meta);
	}
	
	public void refresh(StoredUserTypeMeta meta){
		name = meta.name;
		displayName = meta.caption;
	}

}
