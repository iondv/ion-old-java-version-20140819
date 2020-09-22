package ion.web.app.util;

import ion.core.ICollectionProperty;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IonException;
import ion.viewmodel.navigation.IGroupNode;
import ion.viewmodel.navigation.INode;
import ion.viewmodel.navigation.NodeType;
import ion.web.app.UrlFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

public class BasicNavNode implements INavigationNode {

	private INode base;
	
	private UrlFactory urlfactory;
	
	private Collection<INavigationNode> nodes = null;
	
	public BasicNavNode(INode base, UrlFactory urlfactory) {
		this.base = base;
		this.urlfactory = urlfactory;
	}
	
	@Override
	public String getId(){
		return base.getCode();
	}
	
	@Override
	public String getCaption() {
		return base.getCaption();
	}
	
	@Override
	public String getUrl() {
		try {
			return urlfactory.NodeUrl(base.getCode());
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
	
	public String getUrl(IItem item) {
		if (base instanceof IGroupNode)
			return null;		
		try {
			return urlfactory.Url(base.getCode(),item);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	public String getUrl(IItem item, ICollectionProperty collection) {
		if (base instanceof IGroupNode)
			return null;
		try {
			return urlfactory.Url(base.getCode(),item, collection.getName());
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	public String getUrl(IItem item, String collection) {
		if (base instanceof IGroupNode)
			return null;		
		try {
			return urlfactory.Url(base.getCode(),item, collection);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}	
	
	public String getUrl(String classname, String itemid) {
		if (base instanceof IGroupNode)
			return null;		
		try {
			return urlfactory.Url(base.getCode(),classname,itemid);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getUrl(String classname, String itemid, ICollectionPropertyMeta collection) {
		if (base instanceof IGroupNode)
			return null;		
		try {
			return urlfactory.Url(base.getCode(),classname,itemid, collection.Name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}	
	
	@Override
	public Collection<INavigationNode> getNodes() throws IonException {
		if (nodes == null){
			nodes = new ArrayList<INavigationNode>();
			if (base.getType() == NodeType.GROUP)
				for (INode n: ((IGroupNode)base).getChildNodes()) {
					nodes.add(new BasicNavNode(n,urlfactory));
				}
		}
		return nodes;
	}
}
