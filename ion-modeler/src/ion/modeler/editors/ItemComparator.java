package ion.modeler.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class ItemComparator extends ViewerComparator {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof EditorItemNode && e2 instanceof EditorItemNode){
			EditorItemNode n1 = (EditorItemNode)e1;
			EditorItemNode n2 = (EditorItemNode)e2;
			if(n1.Item instanceof Comparable)
				return ((Comparable)n1.Item).compareTo(n2.Item);
		}
		return 0;
	}

}
	
