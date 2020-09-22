package ion.modeler.editors;

import java.util.Map;

import ion.modeler.forms.IKeyValueProvider;

public class ClassListProvider implements IKeyValueProvider {

	IonEditor editor;
	
	public ClassListProvider(IonEditor e) {
		editor = e;
	}

	@Override
	public Map<String, String> Provide(Object model) {
		return editor.getClassSelection();
	}

}
