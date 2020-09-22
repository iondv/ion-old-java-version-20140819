package ion.modeler.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public abstract class EditorTreeLabelProvider extends LabelProvider {

	public EditorTreeLabelProvider() {
	}
	
	protected String processLabel(String l){
		if (l == null || l.length() == 0)
			return "?";
		return l;
	}
	
	protected String processRole(String r){
		return r;
	}
	
	protected abstract String getEditorItemNodeText(Object item);
	
	protected abstract Image getEditorItemGroupImage(String code);

	protected abstract Image getEditorItemNodeImage(Object item);
						
	public String getText(Object element) {				    	
    	if (element instanceof EditorItemGroup)
    		return ((EditorItemGroup) element).Text;
    	
    	if (element instanceof EditorItemNode)
    		return processLabel(getEditorItemNodeText(((EditorItemNode) element).Item));
    	
    	return "";
      };

      public Image getImage(Object element) {
    	  /*return PlatformUI.getWorkbench().getSharedImages()
    	            .getImage(ISharedImages.IMG_OBJ_ELEMENT);*/
    	  if (element instanceof EditorItemGroup)
    		  return getEditorItemGroupImage(((EditorItemGroup) element).Type);
    	  
    	  if (element instanceof EditorItemNode)
    		  return getEditorItemNodeImage(((EditorItemNode) element).Item);
    	  
    	  return null;
      };
	

}
