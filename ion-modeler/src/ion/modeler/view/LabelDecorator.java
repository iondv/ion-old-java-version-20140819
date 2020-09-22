package ion.modeler.view;

import ion.modeler.resources.IonModelResource;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class LabelDecorator implements ILabelDecorator {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		Image result = image;
		if (element instanceof IonModelResource){
			try {
				int severity = ((IonModelResource)element).Source().findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				if (severity > 0){
					ImageDescriptor problem = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR);
					ImageDescriptor overlay = new IonProblemOverlay(result, problem.createImage());
					result = overlay.createImage();
					
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public String decorateText(String text, Object element) {
		return text;
	}

}
