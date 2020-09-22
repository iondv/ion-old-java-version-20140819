package ion.core;

public interface IReferencePropertyMeta extends IRelationPropertyMeta {
	IClassMeta ReferencedClass() throws IonException;	
}
