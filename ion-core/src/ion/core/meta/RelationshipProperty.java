package ion.core.meta;

import ion.core.IRelationPropertyMeta;
import ion.core.MetaPropertyType;

public abstract class RelationshipProperty extends PropertyMeta implements
																															 IRelationPropertyMeta {

	private boolean eagerLoading;
	
	protected String semantic;
	
	public RelationshipProperty(String name, String caption, MetaPropertyType type, Short size, Short decimals, Boolean nullable, Boolean read_only, Boolean indexed, Boolean unique, Boolean autofilled, String hint, Object dflt, Integer order_number, Boolean is, boolean el, String semantic) {
		super(name, caption, type, size, decimals, nullable, read_only, indexed, unique, autofilled, hint, dflt, order_number, is);
		eagerLoading = el;
		this.semantic = semantic;
	}

	@Override
	public boolean getEagerLoading() {
		return eagerLoading;
	}
	
	@Override
	public String getSemantic(){
		return semantic;
	}

}
