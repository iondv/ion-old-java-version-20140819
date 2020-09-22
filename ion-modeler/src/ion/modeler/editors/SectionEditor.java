package ion.modeler.editors;

import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;
import ion.viewmodel.navigation.NavigationSectionMode;
import ion.viewmodel.plain.StoredNavSection;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class SectionEditor extends IonEditor {

	public static final String ID = "ion.modeler.editors.sectionEditor";
	
	@Override
	protected Object loadModel(IFile f) throws IOException {
		Composer c = new Composer(f.getProject());
		return c.Read(f.getLocation().toString(), StoredNavSection.class);
	}

	@Override
	protected String mainPageText() {
		return "Секция";
	}

	@Override
	protected String formPartName() {
		if (model != null)
			return "Секция: "+((StoredNavSection)model).caption;
		return "";
	}
	
	@SuppressWarnings("serial")
	protected void formProperties(){
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("caption", "Логическое имя");
		captions.put("mode", "Режим отображения");
		captions.put("tags", "Теги");
		formSettings.put("StoredNavSection", 
		new FormSettings(captions, 
			new HashMap<String, String>(){{
				put("tags","commaSeparated");
			}}, 
			getSelections("StoredNavSection")));
	}
	
	@SuppressWarnings("serial")
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = super.getSelections(classname);
		
		if (classname.equals("StoredNavSection")){
			selections.put("mode", new LinkedHashMap<String, String>(){{
				put(String.valueOf(NavigationSectionMode.MENU.getValue()),"Меню");
				put(String.valueOf(NavigationSectionMode.TOC.getValue()),"Содержание");
				put(String.valueOf(NavigationSectionMode.COMBO.getValue()),"Ниспадающий список");
				put(String.valueOf(NavigationSectionMode.HCOMBO.getValue()),"Иерархия");
			}});
		}
		return selections;
	}
}
