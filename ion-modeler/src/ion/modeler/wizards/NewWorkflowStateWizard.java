package ion.modeler.wizards;

import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.framework.workflow.plain.StoredWorkflowState;
import ion.modeler.editors.IonEditor;

public class NewWorkflowStateWizard extends CreationWizard {
	
	public static final String REGISTRY_ID = "ion.modeler.newWorkflowStateWizard";
	
	private IonEditor caller;
	
	public NewWorkflowStateWizard() {
		super();
    pageName = "New WorkflowState";
    pageTitle = "Новое состояние бизнес процесса";
    pageDescription = "Укажите параметры нового состояние.";	
  }
	
	public void setContext(StoredWorkflowModel model){
		this.context = model;
	}
	
	public void setCaller(IonEditor caller){
		this.caller = caller;
	}
	
	@Override
	protected void formCaptions() {
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
	}

	@Override
  protected void formModel() {
	  StoredWorkflowState newState = new StoredWorkflowState();
	  model = newState;
  }

	@Override
	public boolean performFinish() {
		if (context instanceof StoredWorkflowModel){
			((StoredWorkflowModel)context).states.add((StoredWorkflowState)model);
			caller.refreshCollection("states");
			caller.setDirty();
			return true;
		}else{
			return false;
		}
	}
	
}
