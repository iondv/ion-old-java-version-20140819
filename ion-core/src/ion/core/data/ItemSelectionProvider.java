package ion.core.data;

import ion.core.Condition;
import ion.core.FilterOption;
import ion.core.IItemSelectionProvider;
import ion.core.IonException;
import ion.core.Operation;
import ion.core.Sorting;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class ItemSelectionProvider implements IItemSelectionProvider {
	
	protected Collection<FilterOption> conditions;
	
	protected Collection<Sorting> sorting;
	
	public ItemSelectionProvider(Collection<FilterOption> conditions, Collection<Sorting> sorting){
		this.conditions = conditions;
		this.sorting = sorting;
	}

	@Override
	public Map<String, String> SelectList() throws IonException {
		return null;
	}

	@Override
	public Collection<FilterOption> getConditions() {
		return conditions;
	}

	@Override
	public Collection<Sorting> getSorting() {
		return sorting;
	}
	
	private void processFilterOption(FilterOption fo, Collection<String> result){
		if (fo instanceof Condition)
			result.add(((Condition)fo).Property());
		else if (fo instanceof Operation) {
			for (FilterOption fo1: ((Operation)fo).getOperands())
				processFilterOption(fo1, result);
		}
	}

	@Override
	public Collection<String> Dependencies() {
		Collection<String> result = new LinkedList<String>();
		for (FilterOption fo: this.conditions){
			processFilterOption(fo, result);
		}
		return result;
	}
}
