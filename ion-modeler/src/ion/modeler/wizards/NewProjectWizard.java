package ion.modeler.wizards;

import ion.modeler.NewProjectMessages;
import ion.modeler.projects.ProjectManager;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

	private static final String WIZARD_NAME = "New Ion Model Project"; //$NON-NLS-1$
	private static final String PAGE_NAME = "Ion Model Project Wizard"; //$NON-NLS-1$
	
	private IConfigurationElement _configurationElement;
	
	private WizardNewProjectCreationPage _pageOne;
	
	public NewProjectWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	@Override
	public void addPages() {
	    super.addPages();
	 
	    _pageOne = new WizardNewProjectCreationPage(PAGE_NAME);
	    _pageOne.setTitle(NewProjectMessages.NewProjectWizard_FirstPageTitle);
	    _pageOne.setDescription(NewProjectMessages.NewProjectWizard_FirstPageDescription);
	 
	    addPage(_pageOne);
	}	

	@Override
	public boolean performFinish() {
	    String name = _pageOne.getProjectName();
	    URI location = null;
	    if (!_pageOne.useDefaults()) {
	        location = _pageOne.getLocationURI();
	    }

	    ProjectManager.createProject(name, location);
	    
	    BasicNewProjectResourceWizard.updatePerspective(_configurationElement);
	    
	    return true;		
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		_configurationElement = config;
	}

}
