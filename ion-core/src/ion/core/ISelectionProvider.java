package ion.core;

import java.util.Map;

public interface ISelectionProvider {
	Map<String, String> SelectList() throws IonException;
}
