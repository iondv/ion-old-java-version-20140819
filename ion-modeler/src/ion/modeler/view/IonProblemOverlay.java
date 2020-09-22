package ion.modeler.view;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class IonProblemOverlay extends CompositeImageDescriptor {
	
	Image base;

	Image overlay;
	
	public IonProblemOverlay(Image base, Image overlay){
		this.base = base;
		this.overlay = overlay;
	}
	
	@Override
	protected void drawCompositeImage(int width, int height) {
		 drawImage(base.getImageData(), 0, 0);
		 drawImage(overlay.getImageData(), 0, base.getImageData().height - overlay.getImageData().height);
	}

	@Override
	protected Point getSize() {
		return new Point(base.getImageData().width, base.getImageData().height);
	}

}
