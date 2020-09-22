package ion.viewmodel.navigation;

import java.util.Collection;
import java.util.Set;

public interface INavigationSection {
	String getName();
	String getCaption();
	Collection<INode> getRootNodes();
	NavigationSectionMode getMode();
	Set<String> getTags();
}
