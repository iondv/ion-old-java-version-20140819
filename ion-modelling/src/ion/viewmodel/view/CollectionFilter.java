package ion.viewmodel.view;

import java.util.Collection;

public class CollectionFilter {
	private String name;
	
	private Collection<CollectionFilterOption> options;
	
	public CollectionFilter(String name, Collection<CollectionFilterOption> opts){
		this.name = name;
		this.options = opts;
	}
	
	public String getName(){
		return name;
	}
	
	public Collection<CollectionFilterOption> getOptions(){
		return options;
	}
}
