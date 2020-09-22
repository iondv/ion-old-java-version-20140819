package ion.modeler.wizards;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import ion.framework.meta.plain.StoredClassMeta;
import ion.modeler.Composer;
import ion.modeler.resources.IonEntityResource;

public class NewEntityWizard extends CreationWizard {

	public NewEntityWizard() {
		super();
	    pageName = "New entity";
	    pageTitle = "Новая доменная сущность";
	    pageDescription = "Укажите параметры новой сущности.";	
	}
	
	protected void formCaptions(){
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
	}

	@Override
	protected void formModel() {
		model = new StoredClassMeta();
		if (context instanceof IonEntityResource)
			((StoredClassMeta)model).ancestor = ((IonEntityResource) context).getName();
	}
	
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		c.save(model);
		return true;
	}	
	
	public boolean readyToSave(){
		return 
				((StoredClassMeta)model).caption != null
				&& ((StoredClassMeta)model).name != null
				&& ((StoredClassMeta)model).caption.length() > 0 
				&& ((StoredClassMeta)model).name.length() > 0;
	}
}
