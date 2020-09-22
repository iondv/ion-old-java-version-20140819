package ion.framework.meta.plain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StoredSelectionProvider {

	public static final String TYPE_SIMPLE = "SIMPLE";
	
	public static final String TYPE_MATRIX = "MATRIX";
	
	public static final String TYPE_HQL = "HQL";
	
	public String type;
	
	public Collection<StoredKeyValue> list;
	
	public Collection<StoredMatrixEntry> matrix;
	
	public Collection<StoredKeyValue> parameters;
	
	public String hq;	
	
	public StoredSelectionProvider(){
		this(new ArrayList<StoredMatrixEntry>());
	}
		
	public StoredSelectionProvider(String q, Collection<StoredKeyValue> p){
		type = TYPE_HQL;
		hq = q;
		parameters = p;
		matrix = new ArrayList<StoredMatrixEntry>();
		list = new ArrayList<StoredKeyValue>();
	}
	
	public StoredSelectionProvider(Collection<StoredMatrixEntry> matrix){
		type = TYPE_MATRIX;
		this.matrix = matrix;
		list = new ArrayList<StoredKeyValue>();
		hq = "";
		parameters = new ArrayList<StoredKeyValue>();
	}	
	
	public StoredSelectionProvider(Map<String, String> list){
		type = TYPE_SIMPLE;
		this.list = new LinkedList<StoredKeyValue>();
		for (Map.Entry<String, String> e: list.entrySet())
			this.list.add(new StoredKeyValue(e.getKey(), e.getValue()));
		matrix = new ArrayList<StoredMatrixEntry>();
		hq = "";
		parameters = new ArrayList<StoredKeyValue>();
	}
	
	public StoredSelectionProvider(List<StoredKeyValue> list){
		type = TYPE_SIMPLE;
		this.list = list;
		matrix = new ArrayList<StoredMatrixEntry>();
		hq = "";
		parameters = new ArrayList<StoredKeyValue>();
	}	
}
