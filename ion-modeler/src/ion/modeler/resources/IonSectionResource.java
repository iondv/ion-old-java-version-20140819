package ion.modeler.resources;

import ion.viewmodel.plain.StoredNavSection;

import org.eclipse.core.resources.IFile;

public class IonSectionResource extends IonFileBasedResource {
	
	public IonSectionResource(IFile src, StoredNavSection section) {
		super(src);
		refresh(section);
	}
	
	public void refresh(StoredNavSection section){
		this.name = section.name;
		this.displayName = section.caption;		
	}
}
