package ion.core;

public interface IUserTypeMeta {
	String getName();
	String getCaption();
	MetaPropertyType getBaseType();
	Short getSize();
	Short getDecimals();
	String getMask();
	String getMaskName();
}
