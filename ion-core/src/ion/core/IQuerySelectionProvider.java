package ion.core;

import java.util.Collection;
import java.util.Map;

public interface IQuerySelectionProvider extends ISelectionProvider {
	String getQuery();
	
	Map<String, Object> getParameters(IItem item) throws IonException;
	
	Collection<String> Dependencies();
}
