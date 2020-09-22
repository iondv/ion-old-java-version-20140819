package ion.web.app.util;

import ion.core.IonException;
import ion.viewmodel.navigation.INavigationModel;
import ion.viewmodel.navigation.INavigationSection;
import ion.viewmodel.navigation.INode;
import ion.web.app.UrlFactory;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

public class NavigationProvider implements INavigationProvider {
	
	INavigationModel navigationModel;

	@Autowired
	UrlFactory urlFactory;
	
	public void setNavigationModel(INavigationModel m) {
		navigationModel = m;
	}
	
	@Override
	public Collection<INavigationNode> RootNodes(String section) throws IonException {
		Collection<INavigationNode> _roots = new ArrayList<INavigationNode>();
		INavigationSection s = navigationModel.getNavigationSection(section);
		if (s != null) {
			for(INode n : s.getRootNodes())
				_roots.add(new BasicNavNode(n,urlFactory));
		}
		return _roots;
	}
}
