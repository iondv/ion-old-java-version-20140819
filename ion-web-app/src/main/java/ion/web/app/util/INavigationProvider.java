package ion.web.app.util;

import ion.core.IonException;

import java.util.Collection;

public interface INavigationProvider {
	Collection<INavigationNode> RootNodes(String section) throws IonException; 
}
