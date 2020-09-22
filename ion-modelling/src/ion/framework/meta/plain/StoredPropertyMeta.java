package ion.framework.meta.plain;

import ion.core.MetaPropertyType;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

public class StoredPropertyMeta implements Comparable<StoredPropertyMeta> {
	public int order_number = 0;
	
	public String name;
	
	public String caption;
	
	public Integer type;

	public Short size;

	public Short decimals;
	
	public Boolean nullable = true;

	public Boolean readonly = false;

	public Boolean indexed = false;
	
	public Boolean unique = false;
	
	public Boolean autoassigned;
	
	public String hint;
	
	public String default_value;
	
	public String ref_class;
	
	public String items_class;
	
	public String back_ref;
	
	public String back_coll;
	
	public String binding;
	
	public String semantic;
	
	public Collection<StoredCondition> sel_conditions;

	public Collection<StoredSorting> sel_sorting;
	
	public StoredSelectionProvider selection_provider;
	
	public Boolean index_search;
	
	public Boolean eager_loading;
	
	public String formula;
	
	public static Object ParseValue(Integer t, String v) throws ParseException{
		if (v == null)
			return v;
		if (v.toString().isEmpty() && t != MetaPropertyType.STRING.getValue())
			return null;
		if (t == MetaPropertyType.BOOLEAN.getValue())
			return Boolean.parseBoolean(v);
		if (t == MetaPropertyType.DATETIME.getValue())
			return DateFormat.getTimeInstance().parse(v);
		if (t == MetaPropertyType.INT.getValue())
			return Integer.parseInt(v);
		if (t == MetaPropertyType.REAL.getValue() || t == MetaPropertyType.DECIMAL.getValue())
			return Float.parseFloat(v);		
		return v;
	}	
			
	public StoredPropertyMeta() {
		this("","",null,null);
	}
	
	public StoredPropertyMeta(String n, String c, Integer t, Short s){
		this(n,c,t,s,(short)0);
	}	
	
	
	public StoredPropertyMeta(String n, String c, Integer t, 
			Short s, Short d){
		this(n,c,t,s,d,true,false,false,false,false,null);
	}	

	public StoredPropertyMeta(String n, String c, Integer t, 
			Short s, Short d, boolean nl, 
			boolean ro, boolean ind, boolean un, boolean af, String dv){
		this(0,n,c,t,s,d,nl,ro,ind,un, af,null,dv,"","","","","",
				new ArrayList<StoredCondition>(),
				new ArrayList<StoredSorting>(), false, false, null, null);
	}	
	
	public StoredPropertyMeta(int on, String n, String c, Integer t,
	                    			Short s, Short d, boolean nl, 
	                    			boolean ro, boolean ind, boolean un, boolean aa, String h, 
	                    			String dv, String rc, String ic, String br, String bc, String b, 
	                    			Collection<StoredCondition> sc, 
	                    			Collection<StoredSorting> srt,
	                    			Boolean is, Boolean el, String sem, String frml){
		this(on,n,c,t,s,d,nl,ro,ind,un,aa,h,dv,rc,ic,br,bc, b,sc,srt,null,is,el, sem, frml);
	}	
	
	public StoredPropertyMeta(int on, String n, String c, Integer t, 
	                    			Short s, Short d, boolean nl, 
	                    			boolean ro, boolean ind, boolean un, boolean aa, String h, 
	                    			String dv, String rc, String ic, String br, String bc, String b, 
	                    			Collection<StoredCondition> sc, 
	                    			Collection<StoredSorting> srt,
	                    			StoredSelectionProvider sp, Boolean is, Boolean el, String sem, String frml){
  		order_number = on;
  		name = n;
  		caption = c;
  		type = t;
  		size = s;
  		decimals = d;
  		nullable = nl;
  		readonly = ro;
  		indexed = ind;
  		unique = un;
  		autoassigned = aa;
  		hint = h;
  		default_value = dv;
  		ref_class = rc;
  		items_class = ic;
  		back_ref = br;
  		back_coll = bc;
  		binding = b;
  		sel_sorting = (srt == null)?new ArrayList<StoredSorting>():srt;
  		sel_conditions = (sc == null)?new ArrayList<StoredCondition>():sc;
  		selection_provider = sp;
  		index_search = is;
  		eager_loading = el;
  		semantic = sem;
  		formula = frml;
	}	
	
	public int compareTo(StoredPropertyMeta pm) {
		return this.order_number - pm.order_number;
	}
	
}
