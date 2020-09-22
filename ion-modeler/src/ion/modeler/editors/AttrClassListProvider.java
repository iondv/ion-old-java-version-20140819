package ion.modeler.editors;

import java.util.Map;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.forms.IKeyValueProvider;

public class AttrClassListProvider implements IKeyValueProvider {

	IonEditor editor;
	
	public AttrClassListProvider(IonEditor e) {
		editor = e;
	}

	@Override
	public Map<String, String> Provide(Object model) {
		StoredPropertyMeta pm = (StoredPropertyMeta)model;
		
		switch (MetaPropertyType.fromInt(pm.type)){
			case STRUCT:return editor.getClassSelection(false,true);
			case COLLECTION:return editor.getClassSelection(true,false);
			case REFERENCE:return editor.getClassSelection(true,false);
			case CUSTOM:return editor.getUserTypeList();
			default:break;
		}
		
		return editor.getClassSelection();
	}

}
