package ion.modeler.wizards;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import ion.modeler.Composer;
import ion.modeler.wizards.CreationWizard;
import ion.viewmodel.plain.StoredNavSection;

public class NewSectionWizard extends CreationWizard {

	public NewSectionWizard() {
		super();
	    pageName = "New section";
	    pageTitle = "Новая секция навигации";
	    pageDescription = "Укажите параметры новой секции навигации.";	
	}

	protected void formCaptions(){
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
	}
	
	@Override
	protected void formModel() {
		model = new StoredNavSection();
	}

	public boolean readyToSave(){
		return 
				((StoredNavSection)model).name != null
				&& ((StoredNavSection)model).caption != null
				&& ((StoredNavSection)model).name.length() > 0 
				&& ((StoredNavSection)model).caption.length() > 0;
	}
	
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		c.save(model);
		return true;
	}
}
