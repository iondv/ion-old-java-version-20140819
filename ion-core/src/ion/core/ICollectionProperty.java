package ion.core;

public interface ICollectionProperty extends IProperty{	
	IItem[] getItems() throws IonException;
	long getItemCount() throws IonException;
}
