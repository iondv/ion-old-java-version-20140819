package ion.core;

public interface IReferenceProperty extends IProperty {
	IItem getReferedItem() throws IonException;
}
