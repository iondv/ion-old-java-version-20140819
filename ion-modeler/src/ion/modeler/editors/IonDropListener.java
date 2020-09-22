package ion.modeler.editors;

import ion.modeler.forms.ModelItemForm;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

public class IonDropListener extends ViewerDropAdapter {

	private final IonEditor editor;
	private final Viewer viewer;
	private final ModelItemForm form;
	private final IDragDropProvider dd_provider;

  public IonDropListener(IonEditor editor, Viewer viewer, ModelItemForm form, IDragDropProvider dd_provider) {
    super(viewer);
    this.editor = editor;
    this.viewer = viewer;
    this.form = form;
    this.dd_provider = dd_provider;
  }

  @Override
  public void drop(DropTargetEvent event) {
    int location = this.determineLocation(event);    
    Object target = determineTarget(event);
    Object ns = viewer.getInput();
    Object dropped = null;
    if(ns instanceof List<?>){
    	@SuppressWarnings("unchecked")
			List<Object> nodes = (List<Object>)ns;
    	dropped = dd_provider.OnDrop(nodes, target, event.data.toString(), location);
    }
  	viewer.refresh();
  	editor.setDirty();
  	form.setModel(dropped);
  }
  
  @Override
  public boolean performDrop(Object data) {
    return true;
  }

  @Override
  public boolean validateDrop(Object target, int operation,
      TransferData transferType) {
    return true;    
  }

}
