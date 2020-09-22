package ion.core.data;

import java.util.Map;

import ion.core.ISelectionProvider;

public class SimpleSelectionProvider implements ISelectionProvider {
	protected Map<String, String> selection;
	
	public SimpleSelectionProvider(Map<String, String> selection) {
		this.selection = selection;
	}

	@Override
	public Map<String, String> SelectList() {
		return selection;
	}

}
