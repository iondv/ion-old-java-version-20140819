package ion.core;

public interface ICollectionPropertyMeta extends IRelationPropertyMeta {
	String BackCollection();
	
	String BackReference();
	
	String Binding();
	
	IStructMeta ItemsClass() throws IonException;
}
