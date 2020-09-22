package ion.modeler.wizards;

import ion.modeler.Composer;
import ion.modeler.resources.IonModelResource;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public abstract class CreationWizard extends Wizard implements INewWizard {

	public Object model;
	
	public Object context;
	
	public Map<String, Object> selections;
	
	public Map<String,String> types;
	
	public Map<String,String> captions;
	
	private ItemWizardPage _page;
	
	protected String pageName;
	
	protected String pageTitle;
	
	protected String pageDescription;
	
	public CreationWizard() {
		pageName = "";
		pageTitle = "";
		pageDescription = "";
		selections = new TreeMap<String, Object>();
		types = new TreeMap<String,String>();
		captions = new LinkedHashMap<String,String>();
	}
	
	protected void formSelections(){}

	protected void formTypes(){}

	protected void formCaptions(){}
		
	abstract protected void formModel();
	
	public boolean readyToSave(){
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		context = selection.getFirstElement();
		formSelections();
		formTypes();
		formCaptions();
		formModel();
	}

	@Override
	public void addPages() {
	    super.addPages();
	    _page = new ItemWizardPage(pageName);
	    _page.setTitle(pageTitle);
	    _page.setDescription(pageDescription);
	    addPage(_page);
	}
	
	protected boolean doPerform(Composer c) throws IOException, CoreException{
		return true;
	}
			
	@Override
	public boolean performFinish() {
		Composer c = new Composer(((IonModelResource)context).Source().getProject());
		try {
			if (doPerform(c)){
				return true;
			}
		} catch (Exception e){
			e.printStackTrace();
		}		
		return false;
	}

}
