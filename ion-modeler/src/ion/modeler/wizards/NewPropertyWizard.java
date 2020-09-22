package ion.modeler.wizards;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.Composer;
import ion.modeler.editors.IonEditor;

public class NewPropertyWizard extends CreationWizard {
	
	public static final String REGISTRY_ID = "ion.modeler.newPropertyWizard";
	
	private IonEditor caller;
	private Integer nextOrderNumber;
	
	public class StoredPropertyMetaDummy extends StoredPropertyMeta {
		public String template;
	}
	

	public NewPropertyWizard() {
	    pageName = "New property";
	    pageTitle = "Новый атрибут доменной сущности";
	    pageDescription = "Укажите параметры нового атрибута доменной сущности.";	
	}
	
	protected void formCaptions(){
		captions.put("name", "Системное имя");
		captions.put("caption", "Логическое имя");
		captions.put("type", "Тип");
		captions.put("size", "Размер");
		captions.put("order_number", "Порядковый номер");
		captions.put("template", "Шаблон");
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
		
		try {
			Composer c = caller.getComposer();
			Map<String, Object[]> templates =  c.AttrTemplates(true);
			Map<String, String> s = new LinkedHashMap<String, String>();
			for (Object[] tpl: templates.values()){
				s.put(((StoredPropertyMeta)tpl[1]).name, ((StoredPropertyMeta)tpl[1]).caption);
			}
			selections.put("template", s);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}	

	@Override
	protected boolean doPerform(Composer c) throws IOException, CoreException {
		return true;
	}
	
	@Override
	public boolean performFinish() {
		try {
			if (context instanceof StoredClassMeta){
				StoredPropertyMetaDummy d = (StoredPropertyMetaDummy)model;
				StoredPropertyMeta result = null;
				if (d.template != null && !d.template.isEmpty()){
					Composer c = caller.getComposer();
					result = c.getAttrTemplate(d.template);
					result.order_number = d.order_number;
					if (d.name != null && !d.name.isEmpty())
						result.name = d.name;
					if (d.caption != null && !d.caption.isEmpty())
						result.caption = d.caption;
					if (d.type != null)
						result.type = d.type;
					if (d.size != null)
						result.size = d.size;
				} else {
					result = new StoredPropertyMeta(d.name, d.caption, d.type, d.size);
					result.order_number = d.order_number;
				}			
				
				((StoredClassMeta)context).properties.add(result);
				caller.refreshCollection("properties");
				caller.setPageDetailModel("properties", result);
				//caller.setCollectionInput("properties", ((StoredClassMeta)context).properties);
				caller.setDirty();
			}
			return true;
		} catch (Exception e){
			e.printStackTrace();
		}		
		return false;
	}	
	
	public boolean readyToSave(){
		return 
			((StoredPropertyMetaDummy)model).template != null || 
					((StoredPropertyMeta)model).caption != null
					&& ((StoredPropertyMeta)model).name != null
					&& ((StoredPropertyMeta)model).caption.length() > 0 
					&& ((StoredPropertyMeta)model).name.length() > 0
					&& ((StoredPropertyMeta)model).type != null
					&& !(((StoredClassMeta)context).is_struct && ((StoredPropertyMeta)model).type == MetaPropertyType.STRUCT.getValue());
	}
	
	@SuppressWarnings("unchecked")
	public void setContext(StoredClassMeta context){
		this.context = context;
		if(((StoredClassMeta)this.context).is_struct)
			((LinkedHashMap<String, String>)selections.get("type")).remove(String.valueOf(MetaPropertyType.STRUCT.getValue()));
	}
	
	public void setCaller(IonEditor caller){
		this.caller = caller;
	}
	
	public Integer getNextOrderNumber() {
		return nextOrderNumber;
	}

	public void setNextOrderNumber(Integer nextOrderNumber) {
		this.nextOrderNumber = nextOrderNumber;
	}

	@Override
	protected void formModel() {
		StoredPropertyMeta newModel = new StoredPropertyMetaDummy();
		if(nextOrderNumber != null) newModel.order_number = nextOrderNumber;
		model = newModel;
	}

}
