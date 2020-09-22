package ion.framework.meta;

import ion.core.ConditionType;
import ion.core.IConditionalSelectionProvider;
import ion.core.IItem;
import ion.core.IonException;
/*
import ion.core.MetaPropertyType;
import ion.core.data.ReferenceProperty;
*/
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredMatrixEntry;
import ion.framework.meta.plain.StoredPropertyMeta;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class IonMetaConditionalSelectionProvider implements IConditionalSelectionProvider {
	
	private Collection<StoredMatrixEntry> matrix;
	
	public IonMetaConditionalSelectionProvider(Collection<StoredMatrixEntry> matrix){
		this.matrix = matrix;
	}

	@Override
	public Map<String, String> SelectList() throws IonException {
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Boolean _cmp_cond_v(Object v1, ConditionType t, Object v2){
		if (v1 == null)
				v1 = "";
		if (v2 == null)
			  v2 = "";
		switch (t){
			case EMPTY:return v1.toString().isEmpty();
			case EQUAL:return v1.equals(v2);
			case NOT_EMPTY:return !v1.toString().isEmpty();
			case NOT_EQUAL:return !v1.equals(v2);
			case LESS:return ((Comparable)v1).compareTo(v2) < 0;
			case LESS_OR_EQUAL:return ((Comparable)v1).compareTo(v2) <= 0;
			case MORE:return ((Comparable)v1).compareTo(v2) > 0;
			case MORE_OR_EQUAL:return ((Comparable)v1).compareTo(v2) >= 0;
			case IN:{
				if (v2 instanceof Collection)
					return ((Collection)v2).contains(v1);
				return v1.equals(v2);
			}
			case LIKE:return v1.toString().matches(v2.toString());
			default:return false;
		}
	}		

	@Override
	public Map<String, String> SelectList(IItem item) throws IonException {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (StoredMatrixEntry me : matrix){
			boolean skip = false;
			for (StoredCondition sc : me.conditions){
				/*
				Object v = null;
				if (item.Property(sc.property).getType().equals(MetaPropertyType.REFERENCE)){
					v = ((ReferenceProperty)item.Property(sc.property)).getReferedItem();
					if (v != null)
						v = ((IItem)v).getItemId();
				} else*/
				Object v = item.Get(sc.property);
				
				Object cmpv;
				try {
					cmpv = StoredPropertyMeta.ParseValue(item.Property(sc.property).getType().getValue(), sc.value);
				} catch (ParseException e) {
					throw new IonException(e);
				}
				
				if (!_cmp_cond_v(v,ConditionType.fromInt(sc.operation.intValue()), cmpv)){
					skip = true;
					break;
				}			
			}
			if (!skip){
				for (StoredKeyValue skv: me.result)
					result.put(skv.key, skv.value);
			}
		}
		return result;
	}
	
	@Override
	public Collection<String> Dependencies() {
		Collection<String> result = new LinkedList<String>();
		for (StoredMatrixEntry me: matrix) {
			for (StoredCondition c: me.conditions){
				result.add(c.property);
			}
		}
		return result;
	}
}
