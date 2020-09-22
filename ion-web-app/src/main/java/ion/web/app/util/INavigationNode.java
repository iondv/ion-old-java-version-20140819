package ion.web.app.util;

import ion.core.IonException;

import java.util.Collection;

public interface INavigationNode {
	String getId();
	String getCaption();
	String getUrl();	
	Collection<INavigationNode> getNodes() throws IonException;
}
