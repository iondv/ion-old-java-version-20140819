package ion.web.app.util;

import ion.core.IonException;

import java.util.ArrayList;
import java.util.Collection;

public class SimpleNavNode implements INavigationNode {

	String URL;
	
	String caption;
	
	public SimpleNavNode(String URL, String caption) {
		this.URL = URL;
		this.caption = caption;
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public Collection<INavigationNode> getNodes() throws IonException {
		return new ArrayList<INavigationNode>();
	}

	@Override
	public String getId() {
		return URL;
	}
}
