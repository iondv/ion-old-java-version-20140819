package ion.core;

import java.util.Collection;
import java.util.Map;

public interface IConditionalSelectionProvider extends ISelectionProvider {
	Map<String, String> SelectList(IItem item) throws IonException;
	
	Collection<String> Dependencies();
}
