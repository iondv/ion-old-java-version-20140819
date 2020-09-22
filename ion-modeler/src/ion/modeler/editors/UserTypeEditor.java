package ion.modeler.editors;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredUserTypeMeta;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreeViewer;

public class UserTypeEditor extends IonEditor {
	
	public static final String ID = "ion.modeler.editors.userTypeEditor";
	
	@Override
	protected Object loadModel(IFile file) throws IOException {
		Composer composer = new Composer(file.getProject());
		return composer.Read(file.getLocation().toString(), StoredUserTypeMeta.class);
	}

	@Override
	protected String mainPageText() {
		return "Пользовательский тип";
	}
	
	protected void masterSetup(String name, TreeViewer viewer){
		viewer.setComparator(new ItemComparator());
	}
	
	public static void addTypeSelections(Map<String,Object> selections) {
		Map<String, String> result = new LinkedHashMap<String,String>();
		result.put(String.valueOf(MetaPropertyType.STRING.getValue()),"Строка");
		result.put(String.valueOf(MetaPropertyType.INT.getValue()),"Целое");
		result.put(String.valueOf(MetaPropertyType.REAL.getValue()),"Действительное");
		result.put(String.valueOf(MetaPropertyType.DATETIME.getValue()),"Дата/Время");
		result.put(String.valueOf(MetaPropertyType.DECIMAL.getValue()),"Десятичное");
		selections.put("type", result);
	}
	
	public static Map<String, String> getCaptions() {
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
		captions.put("type", "Базовый тип");
		captions.put("size", "Размер");
		captions.put("decimals", "Точность");
		captions.put("mask", "Маска");
		captions.put("mask_name", "Имя стандартной маски");
		return captions;
	}
	
	@Override
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = super.getSelections(classname);
		
		if (classname.equals(StoredUserTypeMeta.class.getSimpleName())) {
			addTypeSelections(selections);
		}
		return selections;
	}
	
	protected void formProperties(){
		Map<String, String> captions = getCaptions();
		formSettings.put(StoredUserTypeMeta.class.getSimpleName(), new FormSettings(captions, null, getSelections(StoredUserTypeMeta.class.getSimpleName())));
	}

	@Override
	protected String formPartName() {
		if (model != null)
			return "Пользовательский тип: " + ((StoredUserTypeMeta)model).caption;
		return "";
	}
}
