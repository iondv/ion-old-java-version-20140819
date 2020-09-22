package ion.modeler.wizards;

import ion.framework.meta.plain.StoredClassMeta;
import ion.modeler.Composer;
import ion.modeler.resources.IonModelResource;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonViewsResource;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.ViewApplyMode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

public class NewViewWizard extends CreationWizard {

	public NewViewWizard() {
		super();
	    pageName = "New view";
	    pageTitle = "Новое представление";
	    pageDescription = "Укажите параметры нового представления.";	
	}

	protected void formCaptions(){
		captions.put("type", "Тип представления");
		captions.put("className", "Представление класса");
		captions.put("overrideMode", "Режим наложения");
		captions.put("actions", "Стандартные действия");
	}	
	
	@Override
	protected void formTypes() {
		this.types.put("actions", "set");
	}

	@SuppressWarnings("serial")
	protected void formSelections(){
		selections.put("type", new LinkedHashMap<String,String>(){{
			put("list","список объектов");
			put("create","форма создания");
			put("item","форма изменения");
			put("detail", "детализация списка");
		}});
		selections.put("overrideMode", new LinkedHashMap<String, String>(){{
			put(String.valueOf(ViewApplyMode.HIDE.getValue()), "Перекрыть");
			put(String.valueOf(ViewApplyMode.OVERRIDE.getValue()), "Переопределить");
		}});
		selections.put("actions", new LinkedHashMap<String, String>(){{
			for (ActionType type: ActionType.values()) {
				String caption = type.getCaption();
				put(String.valueOf(type.getValue()), caption); 
			}
		}});
	
		Composer c = new Composer(((IonModelResource)context).Source().getProject());
		
		try {
			Map<String, Object[]> classes = c.ClassMetas(true);
			Map<String, String> sl = new LinkedHashMap<String,String>();
			for (Object[] v: classes.values())
				sl.put(((StoredClassMeta)v[1]).name, ((StoredClassMeta)v[1]).caption);
			selections.put("className", sl);		
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	@Override
	protected void formModel() {
		model = new NewViewModel(ActionType.CREATE.getValue() | ActionType.SAVE.getValue() | ActionType.DELETE.getValue());
	}
	
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		String node = "";
		if (context instanceof IonNodeResource)
			node  = ((IonNodeResource) context).getName();
		else if (context instanceof IonViewsResource)
			node = ((IonViewsResource) context).Parent().getName();
		
		NewViewModel m = (NewViewModel)model;
		c.createView(node, m.className, m.type, m.overrideMode, m.actions);
		return true;
	}	
	
	public boolean readyToSave(){
		return 
				((NewViewModel)model).type != null
				&& ((NewViewModel)model).className != null
				&& ((NewViewModel)model).type.length() > 0 
				&& ((NewViewModel)model).className.length() > 0;
	}
}
