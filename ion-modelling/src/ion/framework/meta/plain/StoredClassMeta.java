package ion.framework.meta.plain;

import java.util.ArrayList;
import java.util.Collection;

public class StoredClassMeta {
	
	public Boolean is_struct = false;
	
	public String key;
	
	public String semantic;
	
	public String name;
	
	public String version;
	
	public String caption;
	
	public String ancestor;
	
	public String container;
	
	public String creationTracker;
	
	public String changeTracker;
	
	public int history;
	
	public boolean journaling = false;
	
	public Collection<StoredPropertyMeta> properties;
	
	public Collection<StoredPropertyMeta> getProperties() {
		return properties;
	}
			
	public StoredClassMeta(){
		this(false,"","","","","",new ArrayList<StoredPropertyMeta>(),null,null,"","",0,false);
	}
	
	public StoredClassMeta(Boolean struct, String k, String n, String c, String s, Collection<StoredPropertyMeta> pm){
		this(struct, k,n,"",c,s,pm,null,null,"","",0,false);
	}
	
	public StoredClassMeta(Boolean struct, String k, String n, String c, String s, String ancestor, Collection<StoredPropertyMeta> pm){
		this(struct, k,n,"",c,s,pm,ancestor,"","","",0,false);
	}	
	
	public StoredClassMeta(Boolean struct, String k, String n, String c, String s, Collection<StoredPropertyMeta> pm, String container){
		this(struct, k,n,"",c,s,pm,null,container,"","",0,false);
	}		
	
	public StoredClassMeta(Boolean struct, String k, String n, String v, String c, String s, Collection<StoredPropertyMeta> pm, String anc, String container, String creationTrckr, String changeTrckr, Integer hstry, Boolean j){
		is_struct = struct;
		key = k;
		name = n;
		version = v;
		caption = c;
		semantic = s;
		properties = pm;
		ancestor = anc;
		creationTracker = creationTrckr;
		changeTracker = changeTrckr;
		history = (hstry == null)?0:hstry;
		journaling = (j == null)?false:j;
	}
}

	