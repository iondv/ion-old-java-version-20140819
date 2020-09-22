package ion.core.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils;

import ion.core.ICollectionPropertyMeta;
import ion.core.IDataRepository;
import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.IReferenceProperty;
import ion.core.IReferencePropertyMeta;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;

public class Item implements IItem {
	
	protected String id;
	
	protected Object base;
	
	protected IStructMeta itemClass;
	
	protected Map<String, IProperty> properties;
	
	protected IDataRepository rep;
	
	protected String _string;
	
	public Item(String id, Object base, IStructMeta item_class,
							IDataRepository repository) {
		this.id = id;
		this.base = base;
		this.itemClass = item_class;
		this.rep = repository;
	}

	@Override
	public String getClassName() {
		return itemClass.getName();
	}

	@Override
	public String getItemId() {
		return id;
	}

	public Object Base() {
		return base;
	}

	private IItem getAgregate(String name) throws IonException {
		IProperty p = getProperties().get(name);
		IItem i = null;
		if (p != null) {
			if (p instanceof IReferenceProperty)
				i = ((IReferenceProperty) p).getReferedItem();
		}
		return i;
	}

	protected IProperty createProperty(String name, IPropertyMeta meta) {
		if (meta.Type() == MetaPropertyType.REFERENCE)
			return new ReferenceProperty(name, this, (IReferencePropertyMeta) meta);
		else if (meta.Type() == MetaPropertyType.COLLECTION)
			return new CollectionProperty(name, this, (ICollectionPropertyMeta) meta);
		return new Property(name, this, meta);
	}
	
	protected String formStructPropertyName(IStructPropertyMeta spm, IPropertyMeta pm){
		return spm.Name()+"$"+pm.Name();
	}
	
	private void initProperty(IPropertyMeta pm) throws IonException{
		if (pm.Type() == MetaPropertyType.STRUCT){
			IStructMeta sm = ((IStructPropertyMeta)pm).StructClass();
			while (sm != null){
				for (IPropertyMeta pm1: sm.PropertyMetas().values()){
					String propertyName = pm.Name()+"$"+pm1.Name();
					properties.put(propertyName, createProperty(propertyName, pm1));
				}
				sm = sm.getAncestor();
			}
		} else
				properties.put(pm.Name(), createProperty(pm.Name(), pm));
	}

	private void initClassProps(IStructMeta c) throws IonException {
		for (Map.Entry<String, IPropertyMeta> pair : c.PropertyMetas().entrySet()) {
			initProperty((IPropertyMeta) pair.getValue());
		}
		if (c.getAncestor() != null)
			initClassProps(c.getAncestor());
	}

	protected void initProperties() throws IonException {
		properties = new LinkedHashMap<String, IProperty>();
		initClassProps(itemClass);
	}

	@Override
	public Map<String, IProperty> getProperties() throws IonException {
		if (properties == null)
			initProperties();
		return properties;
	}

	@Override
	public IProperty Property(String name) throws IonException {
		if (name.contains(".")) {
			int dot = name.indexOf(".");
			IItem i = getAgregate(name.substring(0, dot));
			if (i != null)
				return i.Property(name.substring(dot + 1));
		}
		return getProperties().get(name);
	}

	public static Object GetObjectProperty(Object o, String name) {
		Field f;
		try {
			f = o.getClass().getField(name);
			if (f != null)
				return f.get(o);
		} catch (Exception e) {
		}

		Method m;
		try {
			m = o.getClass().getMethod("get" + WordUtils.capitalize(name),
																 new Class[] {});
			return m.invoke(o);
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public Object Get(String name) {
		if (name.contains(".")) {
			try {
				int dot = name.indexOf(".");
				IItem i;
				i = getAgregate(name.substring(0, dot));
				if (i != null)
					return i.Get(name.substring(dot + 1));
			} catch (IonException e) {
				return null;
			}
		}
		return GetObjectProperty(base, name);
	}

	public static void SetObjectProperty(Object o, String name, Object value) {
		boolean assigned = false;
		try {
			Field f = o.getClass().getField(name);
			if (f != null) {
				f.set(o, value);
				assigned = true;
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		if (assigned)
			return;

		try {
			Method m = null;
			if (value != null)
				m = o.getClass().getMethod("set" + WordUtils.capitalize(name),
																	 new Class[] { value.getClass() });
			else
				m = o.getClass().getMethod("set" + WordUtils.capitalize(name));
			m.invoke(o, value);
		} catch (NoSuchMethodException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvocationTargetException e) {
		}
	}

	@Override
	public void Set(String name, Object value) {
		if (name.contains(".")) {
			try {
				int dot = name.indexOf(".");
				IItem i;
				i = getAgregate(name.substring(0, dot));
				if (i != null)
					i.Set(name.substring(dot + 1), value);
			} catch (IonException e) {
			}
		}
		SetObjectProperty(base, name, value);
	}

	@Override
	public IStructMeta getMetaClass() {
		return itemClass;
	}

	public String toString() {
		if (_string == null) {
			_string = "";
			try {
				if (itemClass.Semantic() != null) {
					String semantic = itemClass.Semantic();
					String property;
					int start, len;
					Pattern extractor = Pattern.compile("(\\w[\\w\\.]+)(\\[(\\d+)(,(\\d+))?\\])?");
					Matcher matches;
					String[] parts = semantic.split("\\|");
					for (String part : parts) {
						property = part;
						matches = extractor.matcher(part);
						start = -1;
						len = 0;
						if (matches.find()) {
							property = matches.group(1);
							if (matches.group(3) != null)
								start = Integer.parseInt(matches.group(3));
							if (matches.group(5) != null)
								len = Integer.parseInt(matches.group(5));
							
							IProperty p = Property(property.trim());
							if (p != null)
								property = p.getString();
							else
								property = "";
							_string += (start >= 0) ? ((len > 0) ? property.substring(start,
																																				start
																																						+ len)
																									: property.substring(start))
																		 : property;
						} else
							_string += property;
					}
				} else {
					if (Base().getClass().getMethod("toString").getDeclaringClass()
										.equals(Base().getClass()))
						_string = Base().toString();
					else
						_string = itemClass.getCaption() + "@" + this.getItemId();
				}
			} catch (Exception e) {
			}
		}
		return _string;
	}
}
