package ion.modeler.resources;

import ion.framework.meta.plain.StoredValidatorMeta;

import org.eclipse.core.resources.IFile;

public class IonValidatorResource extends IonFileBasedResource {

	public IonValidatorResource(IFile src, StoredValidatorMeta meta) {
		super(src);
		refresh(meta);
	}
	
	public void refresh(StoredValidatorMeta meta){
		name = meta.name;
		displayName = meta.caption;
	}

}
