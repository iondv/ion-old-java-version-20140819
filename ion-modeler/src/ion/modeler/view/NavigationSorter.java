package ion.modeler.view;

import ion.modeler.resources.IonViewsResource;
import ion.modeler.resources.ModelProjectMeta;
import ion.modeler.resources.ModelProjectUserTypes;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class NavigationSorter extends ViewerSorter {

	public NavigationSorter() {
	}

	public NavigationSorter(Collator collator) {
		super(collator);
	}
	
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		if (e1 instanceof ModelProjectMeta)
			return -1;
		
		if (e2 instanceof ModelProjectMeta)
			return 1;
		
		if (e1 instanceof IonViewsResource)
			return -1;
		
		if (e2 instanceof IonViewsResource)
			return 1;
		
		if (e1 instanceof ModelProjectUserTypes)
			return -1;
		
		if (e2 instanceof ModelProjectUserTypes)
			return 1;
		
        return super.compare(viewer, e1, e2);
    }	
}
