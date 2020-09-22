package ion.core.meta;

import ion.core.ISelectionProvider;
import ion.core.IUserTypeMeta;
import ion.core.IUserTypePropertyMeta;
import ion.core.MetaPropertyType;

public class UserTypePropertyMeta extends PropertyMeta implements IUserTypePropertyMeta {
	private IUserTypeMeta utm;
	public UserTypePropertyMeta(String name, String caption,
			MetaPropertyType type, Short size, Short decimals,
			Boolean nullable, Boolean read_only, Boolean indexed,
			Boolean unique, Boolean autoassigned, String hint, Object dflt,
			ISelectionProvider selection, Integer order_number,
			IUserTypeMeta ut, Boolean is, String formula) {
		super(name, caption, type, size, decimals, nullable, read_only, indexed, unique, autoassigned, hint, dflt, selection, order_number, is, formula);
		this.utm = ut;
	}
	@Override
	public IUserTypeMeta UserType() {
		return utm;
	}
}
