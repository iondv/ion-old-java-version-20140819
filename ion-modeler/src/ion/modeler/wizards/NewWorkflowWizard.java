package ion.modeler.wizards;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.modeler.Composer;

public class NewWorkflowWizard extends CreationWizard{
	
	public NewWorkflowWizard() {
	  super();
	  pageName = "New workflow";
	  pageTitle = "Новый бизнес процесс";
	  pageDescription = "Укажите параметры новго процесса";
  }
	
	protected void formCaptions() {
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
	};
	
	@Override
	protected void formSelections() {
	}

	@Override
  protected void formModel() {
		model = new StoredWorkflowModel();
  }
	
	@Override
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		c.save(model);
		return true;
	}	
	
	@Override
	public boolean readyToSave() {
		StoredWorkflowModel wf = (StoredWorkflowModel) model;
		return (wf.name != null) && (wf.caption != null);
	}
}
