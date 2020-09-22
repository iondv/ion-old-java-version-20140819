package ion.modeler.editors;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.Composer;
import ion.modeler.forms.IKeyValueProvider;

public class ClassPropertiesProvider implements IKeyValueProvider {
	
	Composer c;
	
	String pn;
	
	MetaPropertyType filter;
	
	StoredClassMeta cmeta;
	
	public ClassPropertiesProvider(Composer c, String pn, MetaPropertyType f) {
		this.c = c;
		this.pn = pn;
		this.filter = f;
	}
	
	public ClassPropertiesProvider(Composer c, String pn) {
		this(c,pn,null);
	}
	
	public ClassPropertiesProvider(StoredClassMeta cm, Composer c) {
		this(c,null,null);
		cmeta = cm;
	}
	
	public ClassPropertiesProvider(StoredClassMeta cm) {
		this(cm,null);
	}		

	@Override
	public Map<String, String> Provide(Object model) {
		Map<String,String> result = new LinkedHashMap<String, String>();
		
		if (cmeta != null){
			StoredClassMeta struct = cmeta;
			while (struct != null){
				for (StoredPropertyMeta pm: struct.properties){
					result.put(pm.name, pm.caption);
				}
				if (c != null) {
					try {
						struct = c.getClass(struct.ancestor);
					} catch (IOException e) {
						struct = null;
					}
				} else
					struct = null;
			}
			return result;
		}
		
		try {
			if (model != null){
				String cn = (String)model.getClass().getDeclaredField(pn).get(model);
				if (cn != null && cn.length() > 0){
					StoredClassMeta cm = c.getClass(cn.toString());
					for (StoredPropertyMeta pm : cm.properties)
						if (filter == null || (filter.equals(MetaPropertyType.fromInt(pm.type))) || pm.type == MetaPropertyType.STRUCT.getValue())
							if(pm.type == MetaPropertyType.STRUCT.getValue()){
								StoredClassMeta struct = c.getClass(pm.ref_class);
								while(struct != null){
  								for(StoredPropertyMeta spm : struct.properties)
  									if(filter == null || (filter.equals(MetaPropertyType.fromInt(spm.type))))
  										result.put(pm.name+"$"+spm.name, pm.caption+"."+spm.caption);
  								struct = c.getClass(struct.ancestor);
								}
							} else {
								result.put(pm.name, pm.caption);
							}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}			
		return result;
	}
}
