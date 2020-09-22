package ion.modeler.editors;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public abstract class EditorTreeContentProvider implements ITreeContentProvider {
	
	EditorTreeNode[] roots;

	public EditorTreeContentProvider() {
		
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof Collection<?>)
			return null;
		
		if (element instanceof EditorTreeNode)
			return ((EditorTreeNode) element).Parent;
		
		return null;
	}
	
	protected abstract boolean hasItemNodeChildren(Object element);

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Collection<?>)
			return !((Collection<?>)element).isEmpty();
		
		if (element instanceof EditorItemGroup){
			return true;
		}

		if (element instanceof EditorItemNode)
			return hasItemNodeChildren(((EditorItemNode) element).Item);
		
		return false;
	}

	protected abstract Object[] getItemNodeChildren(Object item);
	
	protected abstract Object[] getItemGroupChildren(Object item, String group);
	
	private EditorTreeNode locateExistence(EditorTreeNode[] prev, Object find){
		if (prev != null){
			for (EditorTreeNode n: prev){
				if (n instanceof EditorItemGroup && ((EditorItemGroup)n).Type.equals(find.toString()))
					return n;
				if (n instanceof EditorItemNode && ((EditorItemNode)n).Item.equals(find))
					return n;
			}
		}
		return null;
	}
	
	
	private EditorTreeNode[] wrapChildren(EditorTreeNode parent, Object[] input, EditorTreeNode[] prev){
		if (input == null)
			return new EditorTreeNode[]{};
		EditorTreeNode[] result = new EditorTreeNode[input.length];
		EditorTreeNode existant;
		int i = 0;
		for (Object b: input){
			existant = locateExistence(prev, b);
			if (existant != null)
				result[i] = existant;
			else if (b instanceof String)
				result[i] = new EditorItemGroup((String)b, parent);
			else
				result[i] = new EditorItemNode(b, parent);
			i++;
		}
		return result;
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Collection<?>){
			roots = wrapChildren(null, ((Collection<?>)parentElement).toArray(),roots);
			return roots;
		}
			
		if (parentElement instanceof EditorItemNode){
			((EditorItemNode)parentElement).Children = wrapChildren((EditorTreeNode)parentElement, getItemNodeChildren(((EditorItemNode) parentElement).Item), ((EditorItemNode)parentElement).Children);
			return ((EditorItemNode) parentElement).Children;
		}
		
		if (parentElement instanceof EditorItemGroup){
			EditorItemNode in = (EditorItemNode)((EditorItemGroup) parentElement).Parent;
			((EditorItemGroup) parentElement).Children = wrapChildren((EditorTreeNode)parentElement, getItemGroupChildren(in.Item, ((EditorItemGroup) parentElement).Type),((EditorItemGroup) parentElement).Children);
			return ((EditorItemGroup) parentElement).Children;
		}
		
		return new Object[]{};
	}	
}
