package ion.framework.meta.plain;

public class StoredSorting {

	public String property;
	
	public int mode = 0;
	
	public StoredSorting(){
		
	}
	
	public StoredSorting(String property, int mode) {
		this.property = property;
		this.mode = mode;
	}

}
