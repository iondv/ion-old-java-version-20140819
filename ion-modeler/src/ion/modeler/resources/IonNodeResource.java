package ion.modeler.resources;

import ion.viewmodel.plain.StoredNavNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class IonNodeResource extends IonFileBasedResource implements Comparable<IonNodeResource> {

	IonViewsResource views;
	
	String parentCode;
	
	IonSectionResource section;
	
	int orderNumber;
	
	public IonNodeResource(IFile src, StoredNavNode node, IonSectionResource section) {
		super(src);
		refresh(node, section);
	}
	
	public void refresh(StoredNavNode node, IonSectionResource section){
		this.name = node.code;
		int ind = this.name.lastIndexOf(".");
		if (ind >= 0)
			parentCode = this.name.substring(0, this.name.lastIndexOf("."));
		else
			parentCode = "";
		this.displayName = node.caption;
		this.section = section;
		this.orderNumber = node.orderNumber;
	}
	
	public String getParentCode(){
		return parentCode;
	}
	
	public IonViewsResource Views(){
		if (views == null){
			IProject p = Source().getProject();
			views = new IonViewsResource(p.getFolder("views/"+name.replace(".", "/")), this);
		}
		return views;
	}
	
	public IonSectionResource getSection(){
		return section;
	}

	@Override
	public int compareTo(IonNodeResource arg0) {
		return this.orderNumber = arg0.orderNumber;
	}

}
