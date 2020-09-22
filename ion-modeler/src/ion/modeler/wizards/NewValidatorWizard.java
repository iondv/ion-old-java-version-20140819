package ion.modeler.wizards;

import ion.framework.meta.plain.StoredValidatorMeta;
import ion.modeler.Composer;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

public class NewValidatorWizard extends CreationWizard {

	public NewValidatorWizard() {
		super();
	    pageName = "New validator";
	    pageTitle = "Новый валидатор";
	    pageDescription = "Укажите параметры нового валидатора.";	
	}

	@Override
	protected void formCaptions() {
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
	}

	@Override
	protected void formSelections() {
	}

	
	@Override
	protected void formModel() {
		model = new StoredValidatorMeta();
	}
	
	@Override
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		c.save(model);
		return true;
	}	
	
	@Override
	public boolean readyToSave() {
		StoredValidatorMeta validator = (StoredValidatorMeta)model; 
		return (validator.name != null) && (!validator.name.isEmpty()) && (validator.caption != null) && (!validator.caption.isEmpty()); 
	}

}
