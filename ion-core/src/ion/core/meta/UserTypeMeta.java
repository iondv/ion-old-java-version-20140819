package ion.core.meta;

import ion.core.IUserTypeMeta;
import ion.core.MetaPropertyType;

public class UserTypeMeta implements IUserTypeMeta {
	private String name;
	private String caption;
	private MetaPropertyType baseType;
	private Short size;
	private Short decimals;
	private String mask;
	private String maskName;
	
	public UserTypeMeta(String name, String caption, MetaPropertyType baseType,
			Short size, Short decimals, String mask, String maskName) {
		this.name = name;
		this.caption = caption;
		this.baseType = baseType;
		this.size = size;
		this.decimals = decimals;
		this.mask = mask;
		this.maskName = maskName;
	}

	public String getName() {
		return name;
	}
	public String getCaption() {
		return caption;
	}
	public MetaPropertyType getBaseType() {
		return baseType;
	}
	public Short getSize() {
		return size;
	}
	public Short getDecimals() {
		return decimals;
	}
	public String getMask() {
		return mask;
	}
	public String getMaskName() {
		return maskName;
	}
}