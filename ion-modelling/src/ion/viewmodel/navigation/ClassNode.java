package ion.viewmodel.navigation;

import java.util.Map;

import ion.core.ListOptions;

public class ClassNode extends Node implements IClassNode {

	protected String classname;
	
	protected ListOptions options;
	
	protected Map<String, String[]> pathChains;
	
	public ClassNode(int onum, String code, String caption, String classname, String hint, ListOptions options, Map<String, String[]> pathChains) {
		super(onum, code, caption, NodeType.CLASS, hint);
		this.classname = classname;
		this.options = options;
		this.pathChains = pathChains;
	}

	@Override
	public ListOptions ListOptions() {
		return options;
	}

	@Override
	public String getClassName() {
		return classname;
	}

	@Override
	public Map<String, String[]> PathChains() {
		return pathChains;
	}

}
