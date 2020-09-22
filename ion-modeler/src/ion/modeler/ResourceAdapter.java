package ion.modeler;

import ion.modeler.resources.IonFileBasedResource;
import ion.modeler.resources.IonModelResource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.PlatformObject;
//import org.eclipse.egit.ui.internal.repository.tree.FileNode;

public class ResourceAdapter implements IAdapterFactory {

	public ResourceAdapter() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (
			(adapterType == IResource.class || adapterType == PlatformObject.class) 
			&& (adaptableObject instanceof IonModelResource))
			return ((IonModelResource)adaptableObject).Source();
		
		if ((adapterType == IFile.class)  && (adaptableObject instanceof IonFileBasedResource)){
			IResource src =	((IonFileBasedResource)adaptableObject).Source();
			if (src instanceof IFile)
				return src;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class[] getAdapterList() {
		return new Class[]{IFile.class, IResource.class, PlatformObject.class};
	}

}
