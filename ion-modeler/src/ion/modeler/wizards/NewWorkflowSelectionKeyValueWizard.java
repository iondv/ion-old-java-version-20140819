package ion.modeler.wizards;

import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.workflow.plain.StoredWorkflowSelectionProvider;

public class NewWorkflowSelectionKeyValueWizard extends CreationWizard {

	public static final String REGISTRY_ID = "ion.modeler.newWorkflowSelectionKeyValueWizard";
	
	public NewWorkflowSelectionKeyValueWizard(){
		super();
    pageName = "New WorkflowSelectionKeyValue";
    pageTitle = "Новый параметр";
    pageDescription = "Укажите новый параметр запроса";	
	}
	
	public void setContext(StoredKeyValue model){
		this.context = model;
	}
	
	@Override
	protected void formCaptions() {
		captions.put("key", "Атрибут");
		captions.put("value", "Значение");
	}
	
	@Override
	protected void formModel() {
		StoredKeyValue newSelection = new StoredKeyValue();
		model = newSelection;
	}
	
	@Override
	public boolean performFinish() {
		if (context instanceof StoredWorkflowSelectionProvider) {
			((StoredWorkflowSelectionProvider)context).parameters.add((StoredKeyValue)model);
//			caller.refreshCollection("states");
//			caller.setDirty();
			return true;
		} else {
			return false;
		}
	}

}
