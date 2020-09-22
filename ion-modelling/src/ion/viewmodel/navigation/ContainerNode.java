package ion.viewmodel.navigation;

import java.util.Map;

import ion.core.ListOptions;

public class ContainerNode extends Node implements IContainerNode {

	protected ListOptions options;
	protected String classname;
	protected String id;
	protected String collection;
	protected Map<String, String[]> pathChains;
	
	public ContainerNode(int onum, String code, String caption, String classname, String id, String collection, String hint, ListOptions options, Map<String, String[]> pathChains) {
		super(onum, code, caption, NodeType.CONTAINER, hint);
		this.classname = classname;
		this.id = id;
		this.collection = collection;
		this.options = options;
		this.pathChains = pathChains;
	}

	@Override
	public ListOptions ListOptions(){
		return options;
	}

	@Override
	public String getContainerClassName(){
		return classname;
	}

	@Override
	public String getContainerId(){
		return id;
	}

	@Override
	public String getCollectionName(){
		return collection;
	}

	@Override
	public Map<String, String[]> PathChains() {
		return pathChains;
	}
}
