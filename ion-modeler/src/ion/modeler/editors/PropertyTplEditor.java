package ion.modeler.editors;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class PropertyTplEditor extends IonEditor {
	
	public static final String ID = "ion.modeler.editors.propertyTemplateEditor";

	@Override
	protected Object loadModel(IFile f) throws IOException {
		Composer c = new Composer(f.getProject());
		return c.Read(f.getLocation().toString(), StoredPropertyMeta.class);
	}
	
	@SuppressWarnings("serial")
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = super.getSelections(classname);
		
		if (classname.equals("StoredPropertyMeta")){
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
			
			selections.put("ref_class", new AttrClassListProvider(this));
			selections.put("items_class", new AttrClassListProvider(this));
			selections.put("back_ref", new ClassPropertiesProvider(this.getComposer(), "items_class", MetaPropertyType.REFERENCE));
			selections.put("back_coll", new ClassPropertiesProvider(this.getComposer(), "items_class", MetaPropertyType.COLLECTION));
		}
		return selections;
	}
	
	@SuppressWarnings("serial")
	protected void formProperties(){
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("caption","Логическое имя");
		captions.put("hint","Подсказка");
		captions.put("type","Тип значений");
		captions.put("order_number", "Порядковый номер");
		captions.put("size","Размер");
		captions.put("decimals","Число знаков после запятой");
		captions.put("nullable","Допустимо пустое значение");
		captions.put("readonly","Только для чтения");
		captions.put("indexed","Индексировать для поиска");
		captions.put("unique","Уникальные значения");
		captions.put("autoassigned","Автозаполнение");
		captions.put("default_value","Значение по умолчанию");
		captions.put("formula","Формула");
		captions.put("ref_class","Класс ссылки");
		captions.put("items_class","Класс коллекции");
		captions.put("semantic","Семантика");
		captions.put("back_ref","Атрибут обратной ссылки");
		captions.put("back_coll","Атрибут обратной коллекции");
		captions.put("binding","Основание коллекции");
		captions.put("index_search","Полнотекстовый поиск");
		captions.put("eager_loading","Жадная загрузка");
		
		
		formSettings.put("StoredPropertyMeta", new FormSettings(captions, new HashMap<String,String>(){
		{
			put("nullable","boolean");
			put("readonly","boolean");
			put("indexed","boolean");
			put("unique","boolean");
			put("autoassigned","boolean");
			put("index_search","boolean");
			put("eager_loading","boolean");
		}}, getSelections("StoredPropertyMeta")));
	}	

	@Override
	protected String mainPageText() {
		return "Шаблон атрибута";
	}

	@Override
	protected String formPartName() {
		if (model != null)
			return "Шаблон атрибута: "+((StoredPropertyMeta)model).caption;
		return "";
	}

}
