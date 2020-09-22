package ion.modeler.editors;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;


public class IonDragListener implements DragSourceListener {
	
	private final TreeViewer viewer;
	private final IDragDropProvider dd_provider;

  public IonDragListener(TreeViewer viewer, IDragDropProvider dd_provider) {
    this.viewer = viewer;
    this.dd_provider = dd_provider;
  }

  @Override
  public void dragSetData(DragSourceEvent event) {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    Object firstElement = selection.getFirstElement();
    
    if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
    	event.data = dd_provider.OnDrag(firstElement);
    }
  }

	@Override
	public void dragFinished(DragSourceEvent arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void dragStart(DragSourceEvent arg0) {
		// TODO Auto-generated method stub		
	}
}
