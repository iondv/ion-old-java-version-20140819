package ion.core;

public class CompositeIndex {
	protected String[] properties;
	
	protected boolean unique = false;
	
	public CompositeIndex(String[] properties, boolean unique){
		this.properties = properties;
		this.unique = unique;
	}
	
	public String[] getProperties(){
		return properties;
	}
	
	public boolean isUnique(){
		return unique;
	}
}
