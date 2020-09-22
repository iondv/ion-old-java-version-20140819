package ion.core;

import java.util.Collection;

public interface IItemSelectionProvider extends ISelectionProvider {
	Collection<String> Dependencies();
	
	Collection<FilterOption> getConditions();
	
	Collection<Sorting> getSorting();
}
