package ion.viewmodel.view;

public interface IDataField extends IField {
	String getProperty();
	FieldSize getSize();	
	String getMaskName();
	String getMask();
}
