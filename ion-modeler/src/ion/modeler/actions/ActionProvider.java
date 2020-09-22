package ion.modeler.actions;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class ActionProvider extends CommonActionProvider {
	
	IonOpenAction openAction;
	
	public ActionProvider() {
	}
	
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		openAction = new IonOpenAction();
		openAction.setEnabled(true);
	}

    public void fillActionBars(IActionBars actionBars) {
    	actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
    }	

}
