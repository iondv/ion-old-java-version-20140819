package ion.viewmodel.navigation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GroupNode extends Node implements IGroupNode {
	
	List<INode> children;

	public GroupNode(int onum, String code, String caption, String hint) {
		super(onum, code, caption, NodeType.GROUP, hint);
		children = new LinkedList<INode>();
	}
	
	public void AddChild(INode n){
		children.add(n);
		Collections.sort(children);
	}

	@Override
	public Collection<INode> getChildNodes() {
		return children;
	}

}
