package ion.viewmodel.navigation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NavigationSection implements INavigationSection {
	
	protected String caption;
	
	protected List<INode> nodes;
	
	protected String name;
	
	protected NavigationSectionMode mode;
	
	protected Set<String> tags;
	
	public NavigationSection(String n, String c, List<INode> ns, NavigationSectionMode m, Set<String> tgs){
		this.caption = c;
		this.name = n;
		this.nodes = ns;
		this.mode = m;
		this.tags = tgs;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCaption() {
		return this.caption;
	}

	@Override
	public Collection<INode> getRootNodes() {
		return this.nodes;
	}
	
	public void addRootNode(INode nd) {
		this.nodes.add(nd);
		Collections.sort(this.nodes);
	}

	@Override
	public NavigationSectionMode getMode() {
		return mode;
	}

	@Override
	public Set<String> getTags() {
		return tags;
	}
}
