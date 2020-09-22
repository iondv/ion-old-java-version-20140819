package ion.viewmodel.navigation;

import java.util.Collection;

public interface IGroupNode extends INode {
	
	void AddChild(INode n);
	
	Collection<INode> getChildNodes();
}
