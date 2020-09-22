package ion.web.app.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.IClassMeta;
import ion.core.ICollectionProperty;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IMetaRepository;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.data.ReferenceProperty;
import ion.viewmodel.navigation.IClassNode;
import ion.viewmodel.navigation.IContainerNode;
import ion.viewmodel.navigation.IGroupNode;
import ion.viewmodel.navigation.IListNode;
import ion.viewmodel.navigation.INavigationModel;
import ion.viewmodel.navigation.INode;
import ion.viewmodel.navigation.NodeType;
import ion.web.app.UrlFactory;
import ion.web.app.service.IonWebAppService;

public class PageContext {
	
	public IStructMeta Class;
	
	public String[] SubClasses;
	
	public String Id;
	
	public ICollectionPropertyMeta CollectionProperty;
	
	public ListOptions ListOptions;
	
	public Map<String, String[]> PathChains;
	
	private INavigationNode[] breadcrumbs;
	
	private BasicNavNode node;
	
	private IMetaRepository meta;
	
	private IStructMeta calcNewGroupRoot(IStructMeta proot, IStructMeta nc) throws IonException{
		if (nc.checkAncestor(proot.getName()) == proot)
			return proot;
		else if (proot.getAncestor() != null){
			return calcNewGroupRoot(proot.getAncestor(), nc);
		}
		return null;
	}

	public PageContext(String node, String id, String collection, IMetaRepository meta, INavigationModel nav, UrlFactory uf, IonWebAppService service) throws IonException {
		this.meta = meta;
		
		if (node != null){
			INode n = nav.getNode(node.replace(":", "."));
			if (n != null) {
  			this.node = new BasicNavNode(n,uf);
  			
  			if (!service.CheckNavAccess(this.node.getId()))
  				throw new IonException("Доступ запрещен");
  			
  			List<String> sclist = new LinkedList<String>();
  			IStructMeta gnroot = null;
  			IStructMeta gnrootcandidate = null;
  			IStructMeta nclass = null;
  			
  			PathChains = new HashMap<String, String[]>();
  			
  			if (n.getType() == NodeType.CLASS || n.getType() == NodeType.CONTAINER) {
  				try {
  					ListOptions = ((IListNode) n).ListOptions().clone();
  				} catch (CloneNotSupportedException e) {
  					throw new IonException(e);
  				}
  				PathChains = ((IListNode) n).PathChains();
  			} else if (n.getType() == NodeType.GROUP) {
  				ListOptions = new ListOptions();
  				
  				// XXX: Возможно стоит сделать поиск вложенных классов рекурсивным
  				
  				boolean granted = false;				
  				Collection<INode> childnodes = ((IGroupNode)n).getChildNodes();
  				for (INode cn: childnodes){
  					if (cn.getType() == NodeType.CLASS){
  						if (service.CheckNavAccess(cn.getCode()))
  							granted = true;
  						
  						nclass = meta.Get(((IClassNode)cn).getClassName());
  						if (gnroot == null){
  							gnroot = nclass;
  							sclist.add(nclass.getName());
  						} else {
  							gnrootcandidate = calcNewGroupRoot(gnroot, nclass);
  							if (gnrootcandidate != null){
  								gnroot = gnrootcandidate;
  								sclist.add(nclass.getName());
  							}
  						}
  					}
  				}
  				
  				if (!service.CheckNavAccess(this.node.getId()) && !granted)
  					throw new IonException("Доступ запрещен");				
  				
  				this.Class = gnroot;
  			}
  			
  			if (sclist.isEmpty())
  				SubClasses = new String[0];
  			else {
  				SubClasses = sclist.toArray(new String[sclist.size()]);
  				try {
  					ListOptions.Filter().add(new Condition("class", ConditionType.IN, SubClasses));
  				} catch (Exception e) {
  					throw new IonException(e);
  				}
  			}
  			
  			if (n.getType() == NodeType.CLASS)
  				Class = meta.Get(((IClassNode)n).getClassName());
  			
  			if (n.getType() == NodeType.CONTAINER){
  				Class = meta.Get(((IContainerNode)n).getContainerClassName());
  				Class = ((ICollectionPropertyMeta)
  						Class.PropertyMeta(((IContainerNode)n).getCollectionName())
  						).ItemsClass();
  			}
  		} else
  			throw new IonException("Узел навигации не найден!");
		}	
		
		Id = id;
		
		if (id != null && id.contains(":")){
			String cn = id.substring(0, id.indexOf(":"));
			Class = meta.Get(cn);
			Id = id.substring(id.indexOf(":") + 1);
		}
		
		if (Id != null && Id.isEmpty())
			Id = null;
		
		if (Class == null){
			throw new IonException("Указанный для узла навигации доменный класс не найден!");
		}

		if (collection != null){
			CollectionProperty  = (ICollectionPropertyMeta)Class.PropertyMeta(collection);
			/*
			if (CollectionProperty.ListConditions() != null)
				ListOptions.Filter().addAll(CollectionProperty.ListConditions());
			if (CollectionProperty.ListSorting() != null)
				ListOptions.Sorting().addAll(CollectionProperty.ListSorting());
			*/
		}
	}
		
	public PageContext(String node, IMetaRepository meta, INavigationModel nav, UrlFactory uf, IonWebAppService service) throws IonException{
		this(node,null,null,meta,nav,uf,service);
	}
	
	public PageContext(String node, String id, IMetaRepository meta, INavigationModel nav, UrlFactory uf, IonWebAppService service) throws IonException {
		this(node,id,null,meta,nav,uf,service);
	}
	
	public PageContext(IMetaRepository meta, INavigationModel nav, UrlFactory uf, IonWebAppService service, String id) throws IonException {
		this(null, id, null, meta, nav, uf, service);
	}
	
	
	private String[] container(IStructMeta cm) throws IonException{
		if (PathChains.containsKey(cm.getName()))
			return PathChains.get(cm.getName());
		if (cm.getAncestor() != null)
			return container(cm.getAncestor());
		return new String[]{};
	}
	
	private IItem getParentItem(IItem item, String[] cinfo) throws IonException{
		IItem cnt = null;
		if (cinfo != null && cinfo.length > 0)
			cnt = ((ReferenceProperty)item.Property(cinfo[0])).getReferedItem();
		else if ((item.getMetaClass() instanceof IClassMeta) && ((IClassMeta)item.getMetaClass()).ContainerReference() != null)
			cnt = ((ReferenceProperty)item.Property(((IClassMeta)item.getMetaClass()).ContainerReference())).getReferedItem();
		return cnt;
	}
		
	private INavigationNode itemNavNode(IItem item, ICollectionProperty collection){
		if (collection != null)
			return new SimpleNavNode(this.node.getUrl(item,collection), collection.getCaption());				
		return new SimpleNavNode(this.node.getUrl(item), item.toString());		
	}
	// TODO Убрать отсюда зависимость от логики навигации в теме оформления
	private INavigationNode ajaxItemNavNode(IItem item, ICollectionProperty collection){
		if (collection != null)
			return new SimpleNavNode("collection?__node="+this.node.getId()+"&__container="+item.getClassName()+":"+item.getItemId()+"&__collection="+collection.getName(), collection.getCaption());				
		return new SimpleNavNode("item?__node="+this.node.getId()+"&__id="+item.getClassName()+":"+item.getItemId(), item.toString());		
	}
	
	public INavigationNode ParentPage(IItem item) throws IonException{
		String[] ca = container(meta.Get(item.getClassName()));
		IItem cnt = getParentItem(item, ca);
		if (cnt != null)
			return itemNavNode(item, (ca != null && ca.length > 1)?((ICollectionProperty)item.Property(ca[1])):null);
		return null;
	}
	
	private void buildPath(Collection<INavigationNode> result, IItem item, IonWebAppService data, Boolean include, ICollectionProperty collection) throws IonException{
		String[] ca = container(meta.Get(item.getClassName()));
		IItem cnt = getParentItem(item, ca);
		
		if (cnt != null)
			buildPath(result,cnt,data,true,(ca.length > 1)?((ICollectionProperty)cnt.Property(ca[1])):null);
						
		if (include){
			result.add(itemNavNode(item, null));
			if (collection != null)
				result.add(itemNavNode(item, collection));				
		}
	}
	
	private void ajaxBuildPath(Collection<INavigationNode> result, IItem item, IonWebAppService data, ICollectionProperty collection) throws IonException{
		String[] ca = container(meta.Get(item.getClassName()));
		IItem cnt = getParentItem(item, ca);

		// TODO переход к контейнеру через коллекцию
		
		if (cnt != null)
			ajaxBuildPath(result,cnt,data,(ca.length > 1)?((ICollectionProperty)cnt.Property(ca[1])):null);

		result.add(ajaxItemNavNode(item, null));
				
		if (collection != null)
			result.add(ajaxItemNavNode(item, collection));								
	}
	
	public String getLink(IItem item){
		return this.node.getUrl(item);
	}
	
	public String getLink(){
		if (Id != null && CollectionProperty != null)
			return node.getUrl(this.Class.getName(), Id, CollectionProperty);
		if (Id != null)
			return node.getUrl(this.Class.getName(), Id);
		return this.node.getUrl();
	}
	
	public String getLink(IItem item, String collection){
		return this.node.getUrl(item,collection);
	}
		
	public INavigationNode[] BreadCrumbs(IonWebAppService data) throws IonException{
		if (breadcrumbs == null){
			List<INavigationNode> bc = new ArrayList<INavigationNode>();
			bc.add(this.node);
			if (Id != null){
				IItem item = data.getItem(this.Class.getName(),this.Id);
				if (CollectionProperty != null)
					buildPath(bc,item,data,true,(ICollectionProperty)item.Property(CollectionProperty.Name()));
				else
					buildPath(bc,item,data,true,null);
			}
			//Collections.reverse(bc);
			breadcrumbs = bc.toArray(new INavigationNode[bc.size()]);
		}
		return breadcrumbs;
	}
	
	public INavigationNode[] AjaxBreadCrumbs(IonWebAppService data) throws IonException{
		if (breadcrumbs == null){
			List<INavigationNode> bc = new ArrayList<INavigationNode>();
			bc.add(this.node);
			if (Id != null){
				IItem item = data.getItem(this.Class.getName(),this.Id);
				if (CollectionProperty != null)
					ajaxBuildPath(bc,item,data,(ICollectionProperty)item.Property(CollectionProperty.Name()));
				else
					ajaxBuildPath(bc,item,data,null);
			}
			//Collections.reverse(bc);
			breadcrumbs = bc.toArray(new INavigationNode[bc.size()]);
		}
		return breadcrumbs;
		
	}
	
	public BasicNavNode getNode(){
		return node;
	}

}
