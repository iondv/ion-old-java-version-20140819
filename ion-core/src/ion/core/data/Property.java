package ion.core.data;

import ion.core.IConditionalSelectionProvider;
import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.ISelectionProvider;
import ion.core.IonException;
import ion.core.MetaPropertyType;

import java.util.Map;

public class Property implements IProperty{
	
	protected String name;
	
	protected IItem container;
	
	protected IPropertyMeta meta;
	
	public Property(String name, IItem item, IPropertyMeta meta){
		this.name = name;
		container = item;
		this.meta = meta;
	}
	
	@Override
	public String getName() {
		if (name != null)
			return name;
		return meta.Name();
	}
	
	@Override
	public String getCaption() {
		return meta.Caption();
	}
	
	@Override
	public MetaPropertyType getType(){
		return meta.Type();
	}
	
	@Override
	public IItem getItem() {
		return container;
	}
	
	@Override
	public IPropertyMeta Meta() {
		return meta;
	}
	
	@Override
	public Boolean getReadOnly() {
		return meta.ReadOnly();
	}
	
	@Override
	public Boolean getNullable() {
		return meta.Nullable();
	}
	
	@Override
	public void setValue(Object value) throws IonException{
		container.Set(getName(), value);
	}
	
	@Override
	public Object getValue() throws IonException{
		return container.Get(getName());
	}

	@Override
	public Boolean getIndexed() {
		return meta.Indexed();
	}

	@Override
	public Boolean getUnique() {
		return meta.Unique();
	}

	@Override
	public String getString() {
		try {
			Object v = getValue();
			if (v != null){
				ISelectionProvider sp = this.meta.Selection(); 
				if (sp != null){
					Map<String, String> sl = null;
					if (sp instanceof IConditionalSelectionProvider)
						sl = ((IConditionalSelectionProvider)sp).SelectList(container);
					else
						sl = sp.SelectList();
					if (sl != null && !sl.isEmpty())
						if (sl.containsKey(v.toString()))
							return sl.get(v.toString());
				}
				return v.toString();
			}
		} catch (IonException e) {
		}
		return "";
	}

	
}
