package ion.core;

public class Sorting implements Cloneable {
	private String _property;
	
	private SortingMode _mode;
	
	public Sorting(String property, SortingMode mode){
		_property = property;
		_mode = mode;
	}
	
	public Sorting(String property){
		this(property, SortingMode.ASC);
	}
	
	public Sorting clone() throws CloneNotSupportedException {
		return (Sorting)super.clone();
	}	
	
	public SortingMode Mode() {
		return _mode;
	}
	
	public String Property() {
		return _property;
	}
}
