package ion.modeler.wizards;

import ion.modeler.Composer;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonSectionResource;
import ion.viewmodel.navigation.NodeType;
import ion.viewmodel.plain.StoredNavNode;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.CoreException;

public class NewNodeWizard extends CreationWizard {

	public NewNodeWizard() {
		super();
	    pageName = "New node";
	    pageTitle = "Новый узел навигации";
	    pageDescription = "Укажите параметры нового узла навигации.";	
	}

	protected void formCaptions(){
		captions.put("code", "Системное имя");
		captions.put("orderNumber", "Порядковый номер");
		captions.put("caption", "Логическое имя");
		captions.put("type", "Тип");
	}	
	
	@SuppressWarnings("serial")
	protected void formSelections(){
		selections.put("type", new LinkedHashMap<String,String>(){{
			put(String.valueOf(NodeType.GROUP.getValue()),"Группа");
			put(String.valueOf(NodeType.CLASS.getValue()),"Страница класса");
			put(String.valueOf(NodeType.CONTAINER.getValue()),"Страница контейнера");
			put(String.valueOf(NodeType.HYPERLINK.getValue()),"Гиперссылка");
		}});
	}	

	@Override
	protected void formModel() {
		model = new StoredNavNode();
	}
	
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		String section = "";
		if (context instanceof IonNodeResource){
			((StoredNavNode)model).code = ((IonNodeResource) context).getName()+"."+((StoredNavNode)model).code;
			section = ((IonNodeResource) context).getSection().getName();
		} else if (context instanceof IonSectionResource){
			section = ((IonSectionResource) context).getName();
		}
		c.saveNavNode(model, section);
		return true;
	}	
	
	public boolean readyToSave(){
		return 
				((StoredNavNode)model).caption != null
				&& ((StoredNavNode)model).code != null
				&& ((StoredNavNode)model).caption.length() > 0 
				&& ((StoredNavNode)model).code.length() > 0;
	}
}
