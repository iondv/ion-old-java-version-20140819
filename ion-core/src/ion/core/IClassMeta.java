package ion.core;

public interface IClassMeta extends IStructMeta {
	String[] KeyProperties();
	
	String ContainerReference();
	
	String CreationTracker();
	
	String ChangeTracker();
	
	HistoryMode History();
	
	boolean Journaling();
	
	CompositeIndex[] CompositeIndexes();
}
