package ion.framework.meta.plain;

import java.util.ArrayList;
import java.util.Collection;

@Deprecated
public class SelectionProviderOption {
	
	public static final String TYPE_MATRIX = "MATRIX";
	
	public static final String TYPE_HQL = "HQL";
	
	public static final String ROLE_ANY = "ANY";	
	
	public String role;
	
	public String type;
	
	public Collection<StoredMatrixEntry> matrix;
	
	public Collection<StoredKeyValue> parameters;
	
	public String hq;	
	
	public SelectionProviderOption(String role){
		this(new ArrayList<StoredMatrixEntry>(), role);
	}
	
	public SelectionProviderOption(String q, Collection<StoredKeyValue> p){
		this(q, p, ROLE_ANY);
	}
	
	public SelectionProviderOption(Collection<StoredMatrixEntry> matrix){
		this(matrix, ROLE_ANY);
	}	
	
	public SelectionProviderOption(String q, Collection<StoredKeyValue> p, String role){
		this.role = role;
		type = TYPE_HQL;
		hq = q;
		parameters = p;
		matrix = new ArrayList<StoredMatrixEntry>();
	}
	
	public SelectionProviderOption(Collection<StoredMatrixEntry> matrix, String role){
		this.role = role;
		type = TYPE_MATRIX;
		this.matrix = matrix;
		hq = "";
		parameters = new ArrayList<StoredKeyValue>();
	}	
}
