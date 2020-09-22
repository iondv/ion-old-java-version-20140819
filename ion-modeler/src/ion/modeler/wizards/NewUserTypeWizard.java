 package ion.modeler.wizards;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import ion.framework.meta.plain.StoredUserTypeMeta;
import ion.modeler.Composer;
import ion.modeler.editors.UserTypeEditor;

public class NewUserTypeWizard extends CreationWizard {

	public NewUserTypeWizard() {
		super();
	    pageName = "New user type";
	    pageTitle = "Новый пользовательский тип";
	    pageDescription = "Укажите параметры нового пользовательского типа.";	
	}
	
	@Override
	protected void formCaptions() {
		captions.putAll(UserTypeEditor.getCaptions());
	}

	@Override
	protected void formSelections() {
		UserTypeEditor.addTypeSelections(selections);
	}

	
	@Override
	protected void formModel() {
		model = new StoredUserTypeMeta();
	}
	
	@Override
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		c.save(model);
		return true;
	}	
	
	@Override
	public boolean readyToSave() {
		StoredUserTypeMeta type = (StoredUserTypeMeta)model; 
		return (type.name != null) && (!type.name.isEmpty()) && (type.caption != null) && (!type.caption.isEmpty())
				&& (type.type != null); 
	}
	
}
