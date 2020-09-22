package ion.viewmodel.plain;

public class StoredPathChain {

	public String class_name;
	
	public String[] path;

	public StoredPathChain() {
		this("",new String[]{});
	}	
	
	public StoredPathChain(String class_name, String[] path) {
		this.class_name = class_name;
		this.path = path;
	}

}
