package ion.modeler.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;


public abstract class IonModelResource extends PlatformObject {

	IResource source;
	
	protected String name;
	
	protected String displayName;
	
	public IonModelResource(IResource src) {
		source = src;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDisplayName(){
		return displayName;
	}
	
	public String toString(){
		return getDisplayName() + " ["+getName()+"]";
	}
	
	public IResource Source(){
		return source;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(this.getClass()) && this.name.equals(((IonModelResource)obj).name);
	}
}
