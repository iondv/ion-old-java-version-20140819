package ion.modeler.resources;

import ion.framework.meta.plain.StoredPropertyMeta;

import org.eclipse.core.resources.IFile;

public class IonPropertyTemplateResource extends IonFileBasedResource {

	StoredPropertyMeta meta;
	
	public IonPropertyTemplateResource(IFile src, StoredPropertyMeta pm) {
		super(src);
		meta = pm;
		refresh(meta);
	}

	public void refresh(StoredPropertyMeta meta){
		name = meta.name;
		displayName = meta.caption;
	}	
}
