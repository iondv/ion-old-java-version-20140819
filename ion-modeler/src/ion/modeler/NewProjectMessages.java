package ion.modeler;

import org.eclipse.osgi.util.NLS;

public class NewProjectMessages extends NLS {
	private static final String BUNDLE_NAME = "ion.modeler.newprojectmessages"; //$NON-NLS-1$
	public static String NewProjectWizard_FirstPageDescription;
	public static String NewProjectWizard_FirstPageTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, NewProjectMessages.class);
	}

	private NewProjectMessages() {
	}
}
