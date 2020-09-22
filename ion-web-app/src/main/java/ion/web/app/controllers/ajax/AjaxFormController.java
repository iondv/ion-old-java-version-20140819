package ion.web.app.controllers.ajax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.viewmodel.view.IFormViewModel;
import ion.web.app.FormController;
import ion.web.app.IActionHandler;
import ion.web.app.ajax.ActionResponse;
import ion.web.app.ajax.Breadcrumbs;
import ion.web.app.jstl.Urls;
import ion.web.app.service.IonWebAppService;
import ion.web.app.util.INavigationNode;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.app.util.PageContext;
import ion.web.app.util.SimpleNavNode;

public abstract class AjaxFormController extends FormController {
	
	protected abstract IFormViewModel getFormView(String node, IStructMeta cm, IItem item) throws IonException;
	
	protected Map<String,Object> getForm(PageContext context, String node, String cc, String params) throws IonException{
		Map<String,Object> responseMap = new HashMap<String,Object>();
		IItem item = null;
		if(context.Id != null)
			item = data.getItem(cc, context.Id);
		else
			item = data.getDummy(cc);
		
		data.initItem(item, false, 1);
		
		if(params == null || params.contains("item")){
			if(context.CollectionProperty != null){
				IPropertyMeta pm = context.Class.PropertyMeta(context.CollectionProperty.Name());
				if(pm instanceof ICollectionPropertyMeta){
					String br = ((ICollectionPropertyMeta)pm).BackReference();
					if (br != null && !br.isEmpty())
						item.Set(br, idToKey(cc,br, context.Id));
				}
			}
			
			Map<String, Object> itemToMap = data.ItemToMap(item);
			/*
	    //selections
			for(Map.Entry<String, Map<String, String>> sel : 
				data.getPropertiesSelection(cc, context.Id).entrySet()){
				itemToMap.put(sel.getKey()+"__sel", sel.getValue());
			};
			*/
			//item
	    responseMap.put("item", itemToMap);
		}
    
		if(params == null || params.contains("breadcrumbs")){
	    //breadcrumbs
			Collection<Breadcrumbs> breadcrumbsCol = new ArrayList<Breadcrumbs>();
			for(INavigationNode bc : context.AjaxBreadCrumbs(data)){ 
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
			responseMap.put("breadcrumbs", breadcrumbsCol);
		}
		
		if(params == null || params.contains("validators")){
			//validators
			responseMap.put("validators",viewmodel.getValidators());
		}
		
		if(params == null || params.contains("vm")){
			responseMap.put("vm",getFormView(node, item.getMetaClass(), item));
		}
		
		return responseMap;
	}
	
	protected Object idToKey(String classname, String property, String id) throws IonException{
		if (id == null)
			return null;
		IClassMeta cm = (IClassMeta)meta.Get(classname);
		switch (cm.PropertyMeta(property).Type()){
			case BOOLEAN:return (id == "1")?true:false;
			case DATETIME:return new Date(Long.parseLong(id));
			case REAL:
			case DECIMAL:return Float.parseFloat(id);
			case INT:
			case SET:return Integer.parseInt(id);
			default:return id;
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
	    
	    if(handler == null)
	    	handler = getWorkflowHandler(actionName, context, request.getSession());
	    if (handler != null)
	    	return handler.executeAction(context, request);
	    
	    throw new IonException("Не найден обработчик для данного действия!");
	}
	
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

  		if(result instanceof ActionResponse)
  	  	return (ActionResponse)result;

  		return new ActionResponse(result,null);
  	} catch (IonException e){
  		model.asMap().clear();
  		logger.Error("Ошибка выполнения операции.", e);
  		return new ActionResponse(e.getMessage());
  	}
  }

	private IActionHandler getWorkflowHandler(String actionName, PageContext context, HttpSession session) throws IonException {
			final IonWebAppService dt = data;
			final String transitionName = actionName;
			return new IActionHandler() {
				
				@Override
				public Object executeAction(PageContext context,
																		MultipartHttpServletRequest request)
																																				throws IonException {
					try {
			      Map<String, Object> itemData = postedData(context.Class.getName(),LocaleContextHolder.getLocale(),request);
			      IItem item = dt.PerformTransition(transitionName ,context.Class.getName(), context.Id, itemData, 3);
			      return new ActionResponse(data.ItemToMap(item), null, new IonMessage("Действие выполнено успешно", IonMessageType.INFO));
		      } catch (ParseException e) {
		      	throw new IonException(e);
		      }
				}
			};
  }
}
