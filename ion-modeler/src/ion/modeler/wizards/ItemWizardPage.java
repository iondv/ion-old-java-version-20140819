package ion.modeler.wizards;

import java.util.HashMap;

import ion.modeler.forms.FormSettings;
import ion.modeler.forms.ModelItemForm;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ItemWizardPage extends WizardPage implements Listener {

	ModelItemForm frm;
	
	public ItemWizardPage(String pageName) {
		super(pageName);
		this.setPageComplete(false);
	}

	public ItemWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		this.setPageComplete(false);
	}

	@SuppressWarnings("serial")
	@Override
	public void createControl(Composite parent) {
		final CreationWizard w = (CreationWizard)getWizard();
		frm = new ModelItemForm(new HashMap<String, FormSettings>(){{
			put(w.model.getClass().getSimpleName(),new FormSettings(w.captions, w.types, w.selections)
			);
		}});
		frm.SetChangeListener(this);
		frm.setModel(w.model);
		setControl(frm.Build(parent));
	}
	
	@Override
	public void handleEvent(Event event) {
		this.setPageComplete(((CreationWizard)getWizard()).readyToSave());
	}

}
