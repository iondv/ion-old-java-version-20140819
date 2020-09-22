package ion.modeler.wizards;

import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.framework.workflow.plain.StoredWorkflowTransition;
import ion.modeler.editors.IonEditor;

public class NewWorkflowTransitionWizard extends CreationWizard {
	
	public static final String REGISTRY_ID = "ion.modeler.newWorkflowTransitionWizard";
	
	private IonEditor caller;
	
	public NewWorkflowTransitionWizard() {
		super();
    pageName = "New WorkflowTransition";
    pageTitle = "Новый переход бизнес процесса";
    pageDescription = "Укажите параметры нового перехода.";
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
	  StoredWorkflowTransition newTransition = new StoredWorkflowTransition();
	  model = newTransition;
  }
	
	@Override
	public boolean performFinish() {
		if(context instanceof StoredWorkflowModel){
			((StoredWorkflowModel)context).transitions.add((StoredWorkflowTransition)model);
			caller.refreshCollection("transitions");
			caller.setDirty();
			return true;
		}else{
			return false;
		}
	}

}
