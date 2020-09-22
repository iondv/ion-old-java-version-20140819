package ion.core.data;

import ion.core.IItem;
import ion.core.IReferenceProperty;
import ion.core.IReferencePropertyMeta;
import ion.core.IonException;
import ion.core.PrimitiveWrappers;

public class ReferenceProperty extends Property implements IReferenceProperty{
	
	IItem referedItem = null;
	Object prevV = null;
	
	public ReferenceProperty(String name, Item item, IReferencePropertyMeta meta){
		super(name,item,meta);
	}

	@Override
	public IItem getReferedItem() throws IonException{
		Object v = super.getValue();
		if (referedItem == null && (v != prevV)){
			if (v != null){
				String cn = ((IReferencePropertyMeta)Meta()).ReferencedClass().getName();
				if (v.getClass().isPrimitive())
					referedItem = ((Item)container).rep.GetItem(cn,String.valueOf(v));
				else if (PrimitiveWrappers.is(v))
					referedItem = ((Item)container).rep.GetItem(cn,v.toString());
				else
					referedItem = ((Item)container).rep.GetItem(v);
			}
			prevV = v;
		}
		return referedItem;
	}

	@Override
	public Object getValue() throws IonException {
		Object result = super.getValue();
		if (result != null && !result.getClass().isPrimitive()
				&& !PrimitiveWrappers.is(result)) {
			IItem i = getReferedItem();
			if (i != null)
				return i.getItemId();
			return null;
		}
		return result;
	}

	@Override
	public String getString() {
		try {
			IItem v = getReferedItem();
			if (v != null)
				return v.toString();
		} catch (IonException e) {
		}
		return "";
	}
}

