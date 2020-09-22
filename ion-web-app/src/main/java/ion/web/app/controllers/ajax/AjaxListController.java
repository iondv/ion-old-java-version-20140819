package ion.web.app.controllers.ajax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ion.core.DACPermission;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IPropertyMeta;
import ion.core.IReferencePropertyMeta;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.MetaPropertyType;
import ion.core.logging.ILogger;
import ion.viewmodel.navigation.IClassNode;
import ion.viewmodel.navigation.IGroupNode;
import ion.viewmodel.navigation.INode;
import ion.viewmodel.navigation.NodeType;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.DataField;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.IListViewModel;
import ion.viewmodel.view.ListAction;
import ion.web.app.AbstractListController;
import ion.web.app.IActionHandler;
import ion.web.app.ajax.ActionResponse;
import ion.web.app.ajax.Breadcrumbs;
import ion.web.app.ajax.BulkActionResponse;
import ion.web.app.ajax.ModelInfo;
import ion.web.app.ajax.NodeClass;
import ion.web.app.jstl.Urls;
import ion.web.app.util.INavigationNode;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;
import ion.web.app.util.SimpleNavNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
/*
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
*/
@Controller
public class AjaxListController extends AbstractListController {
	
	@Value("${lists.maxPageSize}")	
	private Integer maxPageSize;

	@RequestMapping(value="/spa/list", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest", 
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getAjaxList(@RequestParam(value="__node", required=true) String node, 
												@RequestParam(value="__page", required=false) Integer page,
												@RequestParam(value="__filter", required=false) String options, 
												@RequestParam(value="__sorting", required=false) String sorting,
												Model model, HttpServletResponse response){
		try {
			PageContext context = new PageContext(node, meta, navmodel, urlfactory,data);
			
			ListOptions lo = prepareOptions(context.Class, context, viewmodel.getListViewModel(node, context.Class), 
			                                (page == null)?1:page, options, sorting);
			
			return getJSONSortedList(lo, context.Class.getName());
		} catch (IonException e) {
			logger.Error("Ошибка получения списка!", e);
			return new JSONResponse(e.getMessage());
		}
	}
		
	@RequestMapping(value="/spa/listinfo", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getAjaxListInfo(@RequestParam(value="__node", required=true) String node,
	                          												@RequestParam(value="__filter", required=false) String options, 
	                          												@RequestParam(value="__sorting", required=false) String sorting){
		try {
			PageContext context = new PageContext(node, meta, navmodel, urlfactory,data);
			String classCaption = context.Class.getCaption();
			ListOptions lo = prepareOptions(context.Class, context, 
			                                viewmodel.getListViewModel(node, context.Class), 1, options, sorting);
			Long pagesCount = (data.getListCount(context.Class.getName(), lo) / lo.PageSize()) + 1;
			INavigationNode[] breadcrumbs = context.AjaxBreadCrumbs(data);
			Collection<Breadcrumbs> breadcrumbsCol = new ArrayList<Breadcrumbs>();
			for(INavigationNode bc : breadcrumbs){ 
				try {
					if(bc instanceof SimpleNavNode){
						breadcrumbsCol.add(new Breadcrumbs(Urls.parseNodeId(bc.getId()), bc.getCaption()));
					}else{
						breadcrumbsCol.add(new Breadcrumbs("list?__node="+Urls.parseNodeId(bc.getId()), bc.getCaption()));
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} 
			};
			return new JSONResponse(new ModelInfo(classCaption, pagesCount, breadcrumbsCol));
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	private boolean checkDescCreationAccess(IStructMeta m) throws IonException {
		for (IStructMeta d: m.Descendants(true)){
			if (data.CheckObjectPermissons(d, new DACPermission[]{DACPermission.USE}))
				return true;
			
			if (checkDescCreationAccess(d))
				return true;
		}
		return false;
	}
	
	
	@RequestMapping(value="/spa/list/vm", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getAjaxVm(@RequestParam(value="__node", required=true) String node,
												Model model, HttpServletResponse response){
		try {
			PageContext context = new PageContext(node, meta, navmodel, urlfactory,data);
			IListViewModel vm = super.listSetup(context.Class, node, context, model);
			
			if (!data.CheckObjectPermissons(context.Class, new DACPermission[]{DACPermission.USE}) &&
					!checkDescCreationAccess(context.Class)){
				vm.getActions().remove(ActionType.CREATE.toString());
			};
			
			Gson gson = new GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().create();
			JsonElement result = gson.toJsonTree(vm);
			result.getAsJsonObject().addProperty("hasBulkActions", vm.hasBulkActions());
			return new JSONResponse(result);
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/details", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getDetails(@RequestParam(value="__node", required=true) String node,
	                                             @RequestParam(value="__id", required=true) String id, 
	                                             Model model, HttpServletResponse response){
		try {
			PageContext lcontext = new PageContext(node, null, meta, navmodel, urlfactory, data);
			PageContext dcontext = new PageContext(node, id, meta, navmodel, urlfactory, data);
			IListViewModel vm = listSetup(lcontext.Class, node, lcontext, model);
			IFormViewModel dm = getDetailModel(vm, dcontext.Class);
			response.addDateHeader("Expires", createExpiresTime(Calendar.MINUTE, 30));
			return new JSONResponse(dm);
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/listAction",
			method = { RequestMethod.POST, RequestMethod.HEAD }, 
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody ActionResponse performListAction(
	                       MultipartHttpServletRequest request, 
	                       Model model, 
	                       @RequestParam(value="__node", required=true) String node, 
	                       @RequestParam(value="__action", required=true) String form_action_name
	                     ) throws IonException, ParseException, IOException{
		return handleAction(node, request, model, form_action_name);
	}	
	
	@RequestMapping(value="/spa/collection", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getAjaxCollectionList(@RequestParam(value="__node", required=true) String node,
												@RequestParam(value="__collection", required=false) String collection, 
												@RequestParam(value="__container", required=false) String id,									
												@RequestParam(value="__page", required=false) Integer page,
												@RequestParam(value="__filter", required=false) String options,
												@RequestParam(value="__sorting", required=false) String sorting) throws Exception {
		PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory,data);
		IItem item = null;
		if (context.Id == null)
			item = data.getDummy(context.Class.getName());
		else
			item = data.getItem(context.Class.getName(), context.Id);		

		IListViewModel vm = viewmodel.getCollectionViewModel(node, item.getMetaClass(), collection);
		
		ListOptions lo = prepareOptions(context.CollectionProperty.ItemsClass(), context, vm, (page == null)?1:page, options, sorting);
		
		return getJSONSortedList(lo, item, context.CollectionProperty.Name());
	}
	
	@RequestMapping(value="/spa/collection/vm", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getAjaxCollectionVm(@RequestParam(value="__node", required=true) String node,
												@RequestParam(value="__collection", required=true) String collection, 
												@RequestParam(value="__container", required=true) String id,
												Model model) throws Exception {
		PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory,data);
		IListViewModel vm = collectionListSetup(context.CollectionProperty.ItemsClass(),node,context,model);
		return new JSONResponse(vm);
	}

	private IListViewModel collectionListSetup(IStructMeta cm, String node, PageContext context, Model model) throws IonException{
		IListViewModel vm = super.listSetup(context.Class, node, context, model);
		vm.getActions().put(ActionType.ADD.toString(), new ListAction(ActionType.ADD.toString(), ActionType.ADD.getCaption(), true));
		vm.getActions().put(ActionType.REMOVE.toString(), new ListAction(ActionType.REMOVE.toString(), ActionType.REMOVE.getCaption(), true));
		return vm;
	}
	
	@RequestMapping(value="/spa/collection/info", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getCollectionModelInfo(@RequestParam(value="__node", required=true) String node,
												@RequestParam(value="__collection", required=true) String collection, 
												@RequestParam(value="__container", required=true) String id,
												Model model) throws IonException {
		PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory,data);
		String classCaption = context.CollectionProperty.Caption();
		IItem item = null;
		if (context.Id == null)
			item = data.getDummy(context.Class.getName());
		else
			item = data.getItem(context.Class.getName(), context.Id);		
		
		Long pagesCount = (data.GetCollectionSize(item, 
		                                          collection, new ListOptions()) / maxPageSize)+1;
		INavigationNode[] breadcrumbs = context.AjaxBreadCrumbs(data);
		Collection<Breadcrumbs> breadcrumbsCol = new ArrayList<Breadcrumbs>();
		for(INavigationNode bc : breadcrumbs){ 
			try {
				if(bc instanceof SimpleNavNode){
					breadcrumbsCol.add(new Breadcrumbs(Urls.parseNodeId(bc.getId()), bc.getCaption()));
				}else{
					breadcrumbsCol.add(new Breadcrumbs("list?__node="+Urls.parseNodeId(bc.getId()), bc.getCaption()));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(logger.Out());
				logger.FlushStream(ILogger.ERROR);
			} 
		};
		return new JSONResponse(new ModelInfo(classCaption, pagesCount, breadcrumbsCol));
	}
	
	private void getNodeClasses(INode node, List<IStructMeta> result) throws IonException {
		if (node.getType() == NodeType.GROUP){
			Collection<INode> childnodes = ((IGroupNode)node).getChildNodes();
			for (INode cn: childnodes){
				if (cn.getType() == NodeType.CLASS){	  						
					result.add(meta.Get(((IClassNode)cn).getClassName()));
				} else
					getNodeClasses(cn, result);
			}
		}
	}	
	
	@RequestMapping(value="/spa/creationclasses", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getClassesToChoose(@RequestParam(value="__node", required=true) String node, 
	                                                     @RequestParam(value="__class", required=false) String className,
	                                                     @RequestParam(value="__container", required=false) String container,
	                                                     @RequestParam(value="__collection", required=false) String collection,
	                                                     @RequestParam(value="__ref", required=false) String refProperty){
		ArrayList<NodeClass> classes = new ArrayList<NodeClass>();
		try {
			PageContext context = null;
			List<IStructMeta> roots = new LinkedList<IStructMeta>();
			
			if((className != null) && (refProperty != null)){
				IStructMeta sm = meta.Get(className);
				if (sm != null){
					IReferencePropertyMeta rm = (IReferencePropertyMeta) sm.PropertyMeta(refProperty);
					if (rm != null)
						roots.add(rm.ReferencedClass());
				}
			} else if (container != null && collection != null) {
				context = new PageContext(node, container, collection, meta, navmodel, urlfactory, data);
				IPropertyMeta pm = context.Class.PropertyMeta(collection);
				if (pm instanceof ICollectionPropertyMeta) {
					ICollectionPropertyMeta cpm = (ICollectionPropertyMeta)pm;
					roots.add(cpm.ItemsClass());
				}
			} else if (node != null) {
				INode n = navmodel.getNode(node.replace(":", "."));
				if (n != null){
					if (n.getType() == NodeType.GROUP){
						getNodeClasses(n, roots);
					} else {
						context = new PageContext(node, meta, navmodel, urlfactory,data);
						roots.add(context.Class);
					}
				}
			}
			
			if (roots.isEmpty() && (className != null))
				roots.add(meta.Get(className));
			
			for (IStructMeta sm: roots){
				if (data.CheckClassInstanciationAccess(sm.getName()))
					classes.add(new NodeClass(sm.getName(), sm.getCaption()));
				
  				for(IStructMeta des : sm.Descendants()){
  					if (data.CheckClassInstanciationAccess(des.getName()))
  						classes.add(new NodeClass(des.getName(), des.getCaption()));
  				}
			}
			
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
		return new JSONResponse(classes);

	}
	
//	@RequestMapping(value="/spa/delete", 
//			method = {RequestMethod.POST, RequestMethod.HEAD},
//			headers = "x-requested-with=XMLHttpRequest",
//			produces = "application/json;charset=UTF-8")
//	public @ResponseBody JSONResponse ajaxDelete(@RequestParam(value="ids", required=false) String[] ids, Model model) throws IonException, IOException{
//		return deleteItems(ids, model);
//	}
	
	@RequestMapping(value="/spa/itemslist", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getAjaxItemsList(@RequestParam(value="__node", required=true) String node, 
	                      @RequestParam(value="__container", required=false) String id,
	                      @RequestParam(value="__collection", required=false) String collection,
												@RequestParam(value="__pagesize", required=false) Integer pagesize,
												@RequestParam(value="__page", required=false) Integer page,
												@RequestParam(value="__count", required=false) Integer count,
												@RequestParam(value="__filter", required=false) String options,
												@RequestParam(value="__sorting", required=false) String sorting) {
		try {
			PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory,data);
			Collection<Map<String, Object>> list = null;
			ListOptions lo = context.ListOptions;
			
			if (context.Id != null){
				IItem item = data.getItem(context.Class.getName(), context.Id);
				IStructMeta cm = context.CollectionProperty.ItemsClass();
				lo = prepareOptions(cm, context, viewmodel.getListViewModel(node, cm), (page == null)?1:page, options, sorting);
				list = getRawList(lo, cm.getName(), item, collection, null);
			} else {
				lo = prepareOptions(context.Class, context, viewmodel.getListViewModel(node, context.Class), (page == null)?1:page, options, sorting);
				list = getRawList(lo, context.Class.getName(), null, null, null);
			}
			List<Object> result = new ArrayList<Object>();
			if(count == null) count = 0;
			if(list.size()>count){
				Object[] listArray = list.toArray();
				for(int i = count; i < listArray.length; i++){
					result.add(listArray[i]);
				}
			}
			return new JSONResponse(result);
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/list/search/vm", 
									method = {RequestMethod.POST, RequestMethod.HEAD},
									headers = "x-requested-with=XMLHttpRequest")
	public @ResponseBody JSONResponse getSearchVm(
	                      @RequestParam(value="__node", required=true) String node){
		try {
			PageContext context = new PageContext(node, meta, navmodel, urlfactory,data);
			List<DataField> pmList = new ArrayList<DataField>();
			for(Entry<String,IPropertyMeta> entry : context.Class.PropertyMetas().entrySet()){
				if(entry.getValue().Type() != MetaPropertyType.COLLECTION && 
						entry.getValue().Type() != MetaPropertyType.TEXT &&
						entry.getValue().Type() != MetaPropertyType.IMAGE &&
						entry.getValue().Type() != MetaPropertyType.FILE){
					pmList.add(new DataField(entry.getValue().Caption(), entry.getValue().Name(), defaultFieldType(entry.getValue()), false));
				}
			};
			return new JSONResponse(pmList);
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/selectionItems", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest", 
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getSelectionItems(@RequestParam(value="__id", required=true) String id,
	                                                    @RequestParam(value="__property", required=true) String property,
	                            												@RequestParam(value="__page", required=false) Integer page,
	                            												@RequestParam(value="__filter", required=false) String options, 
	                            												@RequestParam(value="__sorting", required=false) String sorting){
		try {
			PageContext context = new PageContext(null, id, null, meta, navmodel, urlfactory,data);
			IItem item = null;
			if (context.Id == null)
				item = data.getDummy(context.Class.getName());
			else
				item = data.getItem(context.Class.getName(), context.Id);
			
			IStructMeta cm = null;
			IPropertyMeta pm = item.Property(property).Meta();
			if (pm instanceof IReferencePropertyMeta)
				cm = ((IReferencePropertyMeta)pm).ReferencedClass();
			else if (pm instanceof ICollectionPropertyMeta)
				cm = ((ICollectionPropertyMeta)pm).ItemsClass();
			
			ListOptions lo = prepareOptions(cm,context, viewmodel.getListViewModel(cm), (page == null)?1:page, options, sorting);
			
			return getJSONSortedList(item, property, lo);
		} catch (IonException e) {
			logger.Error("Ошибка получения списка!", e);
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/selection/vm", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getSelectionVm(
	                      @RequestParam(value="__id", required=true) String id,
	                      @RequestParam(value="__property", required=true) String property,
												Model model, HttpServletResponse response){
		try {
			PageContext context = new PageContext(null, id, null, meta, navmodel, urlfactory, data);
			IPropertyMeta pm = context.Class.PropertyMeta(property);
			IStructMeta c = null;
			if (pm instanceof IReferencePropertyMeta)
				c = ((IReferencePropertyMeta)pm).ReferencedClass();
			else if (pm instanceof ICollectionPropertyMeta)
				c = ((ICollectionPropertyMeta)pm).ItemsClass();
			IListViewModel vm = super.listSetup(c, "", null, model);
			return new JSONResponse(vm);
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/selection/listinfo",
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getSelectionListInfo(
                 	      @RequestParam(value="__id", required=true) String id,
                	      @RequestParam(value="__property", required=true) String property,
												@RequestParam(value="__filter", required=false) String options, 
												@RequestParam(value="__sorting", required=false) String sorting){
		try {
			PageContext context = new PageContext(null, id, null, meta, navmodel, urlfactory, data);
			IPropertyMeta pm = context.Class.PropertyMeta(property);
			IStructMeta c = null;
			if (pm instanceof IReferencePropertyMeta)
				c = ((IReferencePropertyMeta)pm).ReferencedClass();
			else if (pm instanceof ICollectionPropertyMeta)
				c = ((ICollectionPropertyMeta)pm).ItemsClass();
			
			String classCaption = c.getCaption();
			IItem item = null;
			if (context.Id == null)
				item = data.getDummy(context.Class.getName());
			else
				item = data.getItem(context.Class.getName(), context.Id);

			ListOptions lo = prepareOptions(c, context, viewmodel.getListViewModel(c), 1, options, sorting);
						
			Long pagesCount = (data.getPropertySelectionItemCount(item, property, new ListOptions()) / lo.PageSize()) + 1;
			return new JSONResponse(new ModelInfo(classCaption, pagesCount, null));
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}

	private Collection<Map<String, Object>> getRawList(
	            ListOptions lo, 
	            String className,
	            IItem item,
	            String collection,
	            String property
	         ) throws IonException {

		Collection<IItem> list = null;
		if (className != null)
				list = data.List(className,lo);
		else if (item != null && collection != null)
				list = data.GetCollection(item, collection, lo);
		else if (item != null && property != null)
				list = data.getPropertySelectionItems(item, property, lo);
		
		final Collection<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		if (list != null){
			for (IItem i : list){
				data.initItem(i, true);
				result.add(data.ItemToMap(i));
			}
		}
		return result;
	}
	
	private JSONResponse getJSONSortedList(ListOptions lo, String className) throws IonException{
		JSONResponse response = new JSONResponse(getRawList(lo,className,null,null, null));
		return response;
	}
	
	private JSONResponse getJSONSortedList(ListOptions lo, IItem item, String collection) throws IonException{
		JSONResponse response = new JSONResponse(getRawList(lo,null,item,collection, null));
		return response;
	}
	
	private JSONResponse getJSONSortedList(IItem item, String property, ListOptions lo) throws IonException{
		JSONResponse response = new JSONResponse(getRawList(lo, null,item, null, property));
		return response;
	}	
	
	
	private long createExpiresTime(int field, int ammount){
		Date expires = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(expires); 
		c.add(field, ammount);
		expires = c.getTime();
		return expires.getTime();
	}
	
	private List<String> deleteItems(String[] ids, String[] collectionId) throws IonException, IOException{
			List<String> deletedIds = new ArrayList<String>();
			for (String id: ids){
				String[] itemId = URLDecoder.decode(id,"UTF-8").split("@");
				if (collectionId == null)
					data.Delete(itemId[0], itemId[1]);
				else
					data.Eject(collectionId[0], collectionId[1], collectionId[2], itemId[0], itemId[1]);
				deletedIds.add(itemId[0]+'@'+itemId[1]);
			}
			return deletedIds;
	}	
/*	
	private Object processJsonElement(JsonElement obj){
		if (obj.isJsonNull())
			return null;
		else if (obj.isJsonObject()){
			Map<String, Object> result = new LinkedTreeMap<String, Object>();
			Set<Entry<String, JsonElement>> map = obj.getAsJsonObject().entrySet();
			for (Entry<String, JsonElement> entry: map){
				if (entry.getKey().equals("timestamp") && map.size() == 1){
					if (entry.getValue().isJsonPrimitive()){
						Long l = entry.getValue().getAsLong();
						return new Date(l);
					}
				}
				result.put(entry.getKey(), processJsonElement(entry.getValue()));
			}
			return result;
		} else if (obj.isJsonPrimitive()){
			JsonPrimitive p = obj.getAsJsonPrimitive();
			if (p.isNumber()){
				Long l = p.getAsLong();
				Double d = p.getAsDouble();
				Object r = 0;
				if (l.doubleValue() == d.doubleValue())
					r = l;
				else
					r = d;
				return r;
			} else if (p.isBoolean())
				return p.getAsBoolean();
			else if (p.isString())
				return p.getAsString();
		} else if (obj.isJsonArray()){
			List<Object> result = new LinkedList<Object>(); 
			Iterator<JsonElement> i = obj.getAsJsonArray().iterator();
			while (i.hasNext()){
				result.add(processJsonElement(i.next()));
			}
			return result.toArray();
		}
		return null;
	}	
*/	
	//actions

  public ActionResponse handleAction(String node, MultipartHttpServletRequest request, 
                                     Model model, String form_action_name) throws IonException, ParseException, IOException {
    try {
    	PageContext context = null;
    	if (request.getParameter("__collection") != null)
    		context = new PageContext(node, request.getParameter("__container"), 
    		                          request.getParameter("__collection"), 
    		                          meta, navmodel, urlfactory, data);
    	else if (request.getParameter("__id") != null)
  			context = new PageContext(node, request.getParameter("__id"), meta, navmodel, urlfactory, data);
  		else
  			context = new PageContext(node, meta, navmodel, urlfactory, data);
  		
  		Object result = execAction(form_action_name, context, request);

  		if(result instanceof BulkActionResponse)
  	  	return (BulkActionResponse)result;
  		
  		if(result instanceof ActionResponse)
  	  	return (ActionResponse)result;

  		return new ActionResponse(result,null);
  	} catch (IonException e){
  		model.asMap().clear();
  		logger.Error("Ошибка выполнения операции.", e);
  		return new ActionResponse(e.getMessage());
  	}
  }
	
	protected Object execAction(String actionName, PageContext context, MultipartHttpServletRequest request) throws IonException {
		IActionHandler handler = null;
	    if (applicationContext.containsBean(actionName)) {
    	    Object bean = applicationContext.getBean(actionName);
    	    if (bean instanceof IActionHandler)
    	        handler = (IActionHandler)bean;
	    } 
	    
	    if (handler == null)
	    	handler = getDefaultHandler(actionName, request.getSession());
	    
	    if (handler != null)
	    	return handler.executeAction(context, request);
	    
	    throw new IonException("Не найден обработчик для данного действия!");
	}
	
  
	protected IActionHandler getDefaultHandler(String actionName, HttpSession session){
		
		final String an = actionName;
		
		if(actionName.equals(ActionType.CREATE.toString()))
				return new IActionHandler() {
					@Override
					public Object executeAction(PageContext context,
					                            MultipartHttpServletRequest request)
					                                                                throws IonException {
						String redirect = null;
						String id = request.getParameter("__id");
						if(id != null && id.contains(":")){
							try {
									String[] itemInfo = id.split(":"); 
		              redirect = "/create?__node="+Urls.parseNodeId(context.getNode().getId()) + "&__class=" + itemInfo[0];
	            } catch (UnsupportedEncodingException e) {
	            	throw new IonException(e.getMessage());
	            }
						}
						if(redirect != null)
							return new ActionResponse(null,redirect);
						else
							throw new IonException("Неправильный запрос");
					}
				};
		
		if (actionName.equals(ActionType.EDIT.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					String redirect = null;
					String id = request.getParameter("__id");
					if(id != null && id.contains(":")){
						try {
	              redirect = "/item?__node="+Urls.parseNodeId(context.getNode().getId()) + "&__id=" + id;
            } catch (UnsupportedEncodingException e) {
            	throw new IonException(e.getMessage());
            }
					}
					if(redirect != null)
						return new ActionResponse(null,redirect);
					else
						throw new IonException("Неправильный запрос");
				}
			};
			
		if (actionName.equals(ActionType.DELETE.toString()) || actionName.equals(ActionType.REMOVE.toString()))
			return new IActionHandler() {
        @Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						if(request.getParameter("ids") != null){
							String[] ids = request.getParameter("ids").split(",");
							String[] col = null;
							if (an.equals(ActionType.REMOVE.toString())){
								col = new String[3];
								String cid = request.getParameter("__container");
								if (cid != null){
									String[] containerInfo = cid.split(":");
									col[0] = containerInfo[0];
									col[1] = containerInfo[1];
									
									if (request.getParameter("__collection") != null)
										col[2] = request.getParameter("__collection");
									else
										throw new IonException("Не указана коллекция!");
									
								} else
									throw new IonException("Не указан идентификатор контейнера!");
							}
							List<String> idsToDelete = deleteItems(ids,col);
							BulkActionResponse res = new BulkActionResponse(idsToDelete, null, null);
							res.setMessage(new IonMessage("Успешно " + (an.equals(ActionType.REMOVE.toString())?"убрано":"удалено"), IonMessageType.INFO));
							return res;
						} else {
							throw new IonException("Не передан массив объектов");
						}					
					} catch (Exception e) {
						throw new IonException(e);
					}
				}
			};
		
		return null;
	}
	
	
}
