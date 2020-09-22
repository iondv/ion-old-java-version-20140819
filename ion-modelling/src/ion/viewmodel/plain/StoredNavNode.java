package ion.viewmodel.plain;

import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredSorting;
import ion.viewmodel.navigation.NodeType;

import java.util.ArrayList;
import java.util.Collection;

public class StoredNavNode implements Comparable<StoredNavNode>{

	public String code;
	
	public Integer orderNumber;
	
	public Integer type;
	
	public String caption;
	
	public String classname;
	
	public String container;
	
	public String collection;
	
	public String url;
	
	public String hint;
	
	public Collection<StoredCondition> conditions;
	
	public Collection<StoredSorting> sorting;
	
	public Collection<StoredPathChain> pathChains;
	
	public StoredNavNode() {
		this(0,"",null,"",null,null,null,null,null,new ArrayList<StoredCondition>(),new ArrayList<StoredSorting>(), new ArrayList<StoredPathChain>());
	}
	
	public StoredNavNode(Integer ordernumber, String code, String caption, String hint) {
		this(ordernumber, code,NodeType.GROUP.getValue(),caption,null,null,null,null,hint,new ArrayList<StoredCondition>(),new ArrayList<StoredSorting>(), new ArrayList<StoredPathChain>());
	}
	
	public StoredNavNode(String url, Integer ordernumber, String code, String caption, String hint) {
		this(ordernumber, code,NodeType.HYPERLINK.getValue(),caption,null,null,null,url,hint,new ArrayList<StoredCondition>(),new ArrayList<StoredSorting>(), new ArrayList<StoredPathChain>());
	}
	
	public StoredNavNode(Integer ordernumber, String code, String caption, String classname, String hint) {
		this(ordernumber, code,NodeType.CLASS.getValue(),caption,classname,null,null,null,hint,new ArrayList<StoredCondition>(),new ArrayList<StoredSorting>(), new ArrayList<StoredPathChain>());
	}
	
	public StoredNavNode(Integer ordernumber, String code, String caption, String classname, String hint, Collection<StoredCondition> conditions) {
		this(ordernumber, code,NodeType.CLASS.getValue(),caption,classname,null,null,null,hint,conditions,new ArrayList<StoredSorting>(), new ArrayList<StoredPathChain>());
	}

	public StoredNavNode(Integer ordernumber, String code, String caption, String classname, String hint, Collection<StoredCondition> conditions, Collection<StoredSorting> sorting) {
		this(ordernumber, code,NodeType.CLASS.getValue(),caption,classname,null,null,null,hint,conditions,sorting, new ArrayList<StoredPathChain>());
	}
	
	public StoredNavNode(Integer ordernumber, String code, String caption, String classname, String container, String collection, String hint) {
		this(ordernumber, code,NodeType.CONTAINER.getValue(),caption,classname,container,collection,null,hint,new ArrayList<StoredCondition>(),new ArrayList<StoredSorting>(), new ArrayList<StoredPathChain>());
	}
	
	public StoredNavNode(Integer ordernumber, String code, String caption, String classname, String container, String collection, String hint, Collection<StoredCondition> conditions) {
		this(ordernumber, code,NodeType.CONTAINER.getValue(),caption,classname,container,collection,null,hint,conditions,new ArrayList<StoredSorting>(), new ArrayList<StoredPathChain>());
	}

	public StoredNavNode(Integer ordernumber, String code, String caption, String classname, String container, String collection, String hint, Collection<StoredCondition> conditions, Collection<StoredSorting> sorting) {
		this(ordernumber, code,NodeType.CONTAINER.getValue(),caption,classname,container,collection,null,hint,conditions,sorting, new ArrayList<StoredPathChain>());
	}

	public StoredNavNode(Integer ordernumber, String code, Integer type, String caption, String classname, String container, String collection, String url, String hint,
			Collection<StoredCondition> conditions, Collection<StoredSorting> sorting,
			Collection<StoredPathChain> pathChains) {
		this.code = code;
		this.orderNumber = (ordernumber==null)?0:ordernumber;
		this.type = type;
		this.caption = caption;
		this.classname = classname;
		this.container = container;
		this.collection = collection;
		this.url = url;
		this.hint = hint;
		this.conditions = conditions;
		this.sorting = sorting;
		this.pathChains = pathChains;
	}

	@Override
	public int compareTo(StoredNavNode o) {
		return this.orderNumber - o.orderNumber;
	}
}
