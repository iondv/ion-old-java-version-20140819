package ion.viewmodel.navigation;

public interface IContainerNode extends IListNode {
	String getContainerClassName();
	String getContainerId();
	String getCollectionName();
}
