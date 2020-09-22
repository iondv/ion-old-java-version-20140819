package ion.modeler.editors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ion.modeler.forms.IKeyValueProvider;
import ion.viewmodel.plain.StoredField;
import ion.viewmodel.view.CollectionFieldMode;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.GeoFieldMode;
import ion.viewmodel.view.ReferenceFieldMode;

public class FieldModeProvider implements IKeyValueProvider {

	public FieldModeProvider() {
	}

	@SuppressWarnings({ "serial", "incomplete-switch" })
	@Override
	public Map<String, String> Provide(Object model) {
		if (model != null && ((StoredField)model).type != null)
		switch (FieldType.fromInt(((StoredField)model).type)){
			case REFERENCE:return new LinkedHashMap<String, String>(){{
				put(String.valueOf(ReferenceFieldMode.LINK.getValue()),"Ссылка");
				put(String.valueOf(ReferenceFieldMode.STRING.getValue()),"Cтрока");
				put(String.valueOf(ReferenceFieldMode.INFO.getValue()),"Форма");
				put(String.valueOf(ReferenceFieldMode.HIERARCHY.getValue()),"Иерархическая ссылка");
			}};
			case COLLECTION:return new LinkedHashMap<String, String>(){{
				put(String.valueOf(CollectionFieldMode.LIST.getValue()),"Список");
				put(String.valueOf(CollectionFieldMode.LINK.getValue()),"Cсылка");
				put(String.valueOf(CollectionFieldMode.LINKS.getValue()),"Список ссылок");
				put(String.valueOf(CollectionFieldMode.TABLE.getValue()),"Таблица");
				put(String.valueOf(CollectionFieldMode.HASHTAGS.getValue()),"Облако тегов");
			}};
			case GEO:return new LinkedHashMap<String, String>(){{
				put(String.valueOf(GeoFieldMode.MAP.getValue()),"Карта");
				put(String.valueOf(GeoFieldMode.LOCATOR.getValue()),"Поиск по адресу");
				put(String.valueOf(GeoFieldMode.CANVAS.getValue()),"Холст");
			}};
		}
		
		return new HashMap<String, String>();
	}

}
