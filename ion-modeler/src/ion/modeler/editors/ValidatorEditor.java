package ion.modeler.editors;

import ion.framework.meta.plain.StoredValidatorMeta;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreeViewer;

public class ValidatorEditor extends IonEditor {

	public static final String ID = "ion.modeler.editors.validatorEditor";
	
	@Override
	protected Object loadModel(IFile file) throws IOException {
		Composer composer = new Composer(file.getProject());
		return composer.Read(file.getLocation().toString(), StoredValidatorMeta.class);
	}

	@Override
	protected String mainPageText() {
		return "Валидатор";
	}
	
	protected void masterSetup(String name, TreeViewer viewer){
		viewer.setComparator(new ItemComparator());
	}
	
	public static Map<String, String> getCaptions() {
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
		captions.put("assignByContainer","Автозаполнять из поля контейнера");
		captions.put("validationExpression", "Выражение ");
		captions.put("mask", "Маска");
		return captions;
	}
	
	@SuppressWarnings("serial")
	protected void formProperties(){
		Map<String, String> captions = getCaptions();
		formSettings.put(StoredValidatorMeta.class.getSimpleName(), new FormSettings(captions, new HashMap<String, String>(){{put("validationExpression","multiline");put("assignByContainer","Boolean");}}, getSelections(StoredValidatorMeta.class.getSimpleName())));
	}

	@Override
	protected String formPartName() {
		if (model != null)
			return "Валидатор: " + ((StoredValidatorMeta)model).caption;
		return "";
	}

}
