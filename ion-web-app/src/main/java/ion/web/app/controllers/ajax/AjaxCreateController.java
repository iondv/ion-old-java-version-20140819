package ion.web.app.controllers.ajax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashSet;

import javax.servlet.http.HttpSession;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IItem;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.ViewApplyMode;
import ion.web.app.IActionHandler;
import ion.web.app.ajax.ActionResponse;
import ion.web.app.jstl.Urls;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;

@Controller
public class AjaxCreateController extends AjaxFormController {
	
	@RequestMapping(value="/spa/dummy", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getDummy(@RequestParam(value="__node", required=true) String node,
											@RequestParam(value="__class", required=false) String className){
		try {
			String cc = "";
			if (className != null) {
				cc = className;
			} else {
				PageContext context = new PageContext(node, meta, navmodel, urlfactory, data);	
				cc = context.Class.getName();
			}
			IItem item = data.getDummy(cc);
			data.initItem(item, true);
			return new JSONResponse(data.ItemToMap(item));
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/createForm", 
			method = { RequestMethod.POST, RequestMethod.HEAD },
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getCreateForm(
		                    @RequestParam(value="__node", required=true) String node, 
		                    @RequestParam(value="__class", required=false) String className,
		                    @RequestParam(value="__container", required=false) String id,
		                    @RequestParam(value="__collection", required=false) String collection,
		                    @RequestParam(value="__params", required=false) String params){
		try {
			PageContext context = new PageContext(node, id, collection, meta, navmodel, urlfactory, data);
			if(className == null){
				if(context.CollectionProperty != null)
					className = context.CollectionProperty.ItemsClass().getName();
				else
					className = context.Class.getName();
			}
			
			if (!data.CheckClassInstanciationAccess(className))
				throw new IonException("Нет прав на создание объектов данного типа!");
			
			return new JSONResponse(getForm(context, node, className, params));
    } catch (IonException e) {
	      return new JSONResponse(e.getMessage());
    }
	}

	@RequestMapping(value="/spa/create/vm", 
			method = { RequestMethod.POST, RequestMethod.HEAD },
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getCreateVm(@RequestParam(value="__node", required=true) String node, 
											@RequestParam(value="__class", required=false) String className){
		try {
			PageContext context = new PageContext(node, meta, navmodel, urlfactory, data);
			String cc = (className == null)?context.Class.getName():className;
			IStructMeta cm = meta.Get(cc);
			return new JSONResponse(getFormView(node, cm, null));
		} catch (IonException e) {
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/create",
			method = { RequestMethod.POST, RequestMethod.HEAD },
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody ActionResponse create(
	                                           MultipartHttpServletRequest request, 
	                                           Model model, 
	                                           @RequestParam(value="__node", required=true) String node, 
	                                           @RequestParam(value="__action", required=true) String form_action_name
	                                           ) throws IonException, ParseException, IOException{
		return handleAction(node, request, model, form_action_name);
	}	
	
	@RequestMapping(value="/spa/createItem",
			method = { RequestMethod.POST, RequestMethod.HEAD },
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse createItem(MultipartHttpServletRequest request) {
		try {
			String requestedItemInfo = request.getParameter("itemInfo");
			if(requestedItemInfo != null){
				String[] itemInfo = requestedItemInfo.split(",");
				String className = itemInfo[0];
				IItem item =data.Create(className, postedData(className,LocaleContextHolder.getLocale(),request), 3);
		    return new JSONResponse(data.ItemToMap(item));
			}else{
				throw new IonException("Неправильный запрос");
			}
    } catch (IonException | ParseException e) {
    	logger.Error("Ошибка редактирования объекта", e);
	    return new JSONResponse(e.getMessage());
    }
	}
	
	protected IActionHandler getDefaultHandler(String actionName, HttpSession session){
		if (actionName.equals(ActionType.CREATE.toString()) || actionName.equals(ActionType.SAVE.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						String cc = request.getParameter("__cc");
						ICollectionPropertyMeta collection = context.CollectionProperty;
						if (cc == null){
							cc = context.Class.getName();
							if (collection != null)
								cc = collection.ItemsClass().getName();
						}
						
						IItem item = data.Create(cc, postedData(cc, LocaleContextHolder.getLocale(), request), 3);
						
						if (collection != null){
							data.Put(context.Class.getName(), context.Id, collection.Name(), cc, item.getItemId());
						}
						
						String redirect = "/item?__node=" + Urls.parseNodeId(context.getNode().getId()) +
															"&__id=" + item.getClassName() + ":" + item.getItemId();
						
						if (collection != null)
							redirect = redirect + "&__container=" + context.Class.getName() + ":" + context.Id + "&__collection=" + collection.Name();

						return new ActionResponse(data.ItemToMap(item), redirect, new IonMessage("Действие выполнено успешно", IonMessageType.INFO));
					} catch (ParseException e) {
						throw new IonException(e);
					} catch (UnsupportedEncodingException e) {
						throw new IonException("");
          }
				}
			};			
			
		if (actionName.equals(ActionType.CANCEL.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						String redirect = "/list?__node="+Urls.parseNodeId(context.getNode().getId());
						return  new ActionResponse(null,redirect);
					} catch (Exception e) {
						throw new IonException(e);
					}
				}
			};			
		
		return null;
	}

	@Override
	protected IFormViewModel getFormView(String node, IStructMeta cm, IItem item)
																																							 throws IonException {
		IFormViewModel vm = viewmodel.getCreationViewModel(node, cm);
		if (vm == null)
			vm = viewmodel.getCreationViewModel(cm);
		
		HashSet<ActionType> defActions = new HashSet<ActionType>();
		
		if (data.CheckClassInstanciationAccess(cm.getName())){
			defActions.add(ActionType.SAVE);
		}
		
		defActions.add(ActionType.CANCEL);
		
		@SuppressWarnings("serial")
		HashSet<FieldType> ignoreFTs = new HashSet<FieldType>(){{add(FieldType.COLLECTION);}};			
		if (vm == null)
			vm = defaultViewModel((IClassMeta)cm, defActions, ignoreFTs);
		if (vm.getMode() == ViewApplyMode.OVERRIDE)
			vm = createOverrideModel(vm, defaultViewModel((IClassMeta)cm, defActions, ignoreFTs));
		return vm;
	}
}