package ion.viewmodel.navigation;

import java.util.Map;

import ion.core.ListOptions;

public interface IListNode extends INode {
	ListOptions ListOptions();
	Map<String, String[]> PathChains();
}
