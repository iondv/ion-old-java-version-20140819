package ion.modeler.wizards;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.Composer;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.CoreException;

public class NewAttrTplWizard extends CreationWizard {
	public static final String REGISTRY_ID = "ion.modeler.newPropertyTplWizard";

	public NewAttrTplWizard() {
	    pageName = "New property template";
	    pageTitle = "Новый шаблон атрибута доменной сущности";
	    pageDescription = "Укажите параметры нового шаблона атрибута доменной сущности.";	
	}
	
	protected void formCaptions(){
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
		captions.put("type", "Тип");
		captions.put("size", "Размер");
	}	
	
	protected void formTypes(){
	}
	
	@SuppressWarnings("serial")
	protected void formSelections(){
		selections.put("type", new LinkedHashMap<String, String>(){{
			put(String.valueOf(MetaPropertyType.STRING.getValue()),"Строка");
			put(String.valueOf(MetaPropertyType.INT.getValue()),"Целое");
			put(String.valueOf(MetaPropertyType.REAL.getValue()),"Действительное");
			put(String.valueOf(MetaPropertyType.BOOLEAN.getValue()),"Логический");
			put(String.valueOf(MetaPropertyType.DATETIME.getValue()),"Дата/Время");
			put(String.valueOf(MetaPropertyType.DECIMAL.getValue()),"Десятичное");
			put(String.valueOf(MetaPropertyType.REFERENCE.getValue()),"Ссылка");
			put(String.valueOf(MetaPropertyType.COLLECTION.getValue()),"Коллекция");
			put(String.valueOf(MetaPropertyType.FILE.getValue()),"Файл");
			put(String.valueOf(MetaPropertyType.IMAGE.getValue()),"Изображение");
			put(String.valueOf(MetaPropertyType.FILESLIST.getValue()),"Коллекция файлов");
			put(String.valueOf(MetaPropertyType.TEXT.getValue()),"Текст");
			put(String.valueOf(MetaPropertyType.HTML.getValue()),"HTML");
			put(String.valueOf(MetaPropertyType.URL.getValue()),"URL");
			put(String.valueOf(MetaPropertyType.GUID.getValue()),"Глобальный идентификатор");
			put(String.valueOf(MetaPropertyType.PASSWORD.getValue()),"Пароль");
			put(String.valueOf(MetaPropertyType.SET.getValue()),"Множество");
			put(String.valueOf(MetaPropertyType.GEO.getValue()),"Геоданные");
			put(String.valueOf(MetaPropertyType.PERIOD.getValue()),"Период");
			put(String.valueOf(MetaPropertyType.STRUCT.getValue()),"Структура");
			put(String.valueOf(MetaPropertyType.CUSTOM.getValue()),"Пользовательский тип");
			put(String.valueOf(MetaPropertyType.USER.getValue()),"Пользователь");
		}});
	}	

	protected boolean doPerform(Composer c) throws IOException, CoreException {
		c.save(model);
		return true;
	}	
	
	public boolean readyToSave(){
		return 
			((StoredPropertyMeta)model).caption != null
			&& ((StoredPropertyMeta)model).name != null
			&& ((StoredPropertyMeta)model).caption.length() > 0 
			&& ((StoredPropertyMeta)model).name.length() > 0
			&& ((StoredPropertyMeta)model).type != null;
	}

	@Override
	protected void formModel() {
		model = new StoredPropertyMeta();
	}
}
