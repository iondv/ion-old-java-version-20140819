package ion.web.app.controllers.ajax;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.servlet.http.HttpSession;
import ion.core.DACPermission;
import ion.core.IClassMeta;
import ion.core.IItem;
import ion.core.IStructMeta;
import ion.core.IWorkflowState;
import ion.core.IWorkflowTransition;
import ion.core.IonException;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.Field;
import ion.viewmodel.view.FieldGroup;
import ion.viewmodel.view.FormAction;
import ion.viewmodel.view.IField;
import ion.viewmodel.view.IFormTab;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.ViewApplyMode;
import ion.web.app.IActionHandler;
import ion.web.app.ajax.ActionResponse;
import ion.web.app.jstl.Urls;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class AjaxEditController extends AjaxFormController {
	
	@RequestMapping(value="/spa/item", 
			method = {RequestMethod.POST, RequestMethod.HEAD},
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getItemAjax(
	                    @RequestParam(value="__node", required=false) String node,
											@RequestParam(value="__id", required=true) String id){
		try {
	    PageContext context = new PageContext(node, id, meta, navmodel, urlfactory, data);
			IItem item = data.getItem(context.Class.getName(),context.Id);
			data.initItem(item, false, 1);
			return new JSONResponse(data.ItemToMap(item));
    } catch (IonException e) {
    	return new JSONResponse(e.getMessage());
    }
	}
	
	@RequestMapping(value="/spa/editForm", 
			method = { RequestMethod.POST, RequestMethod.HEAD },
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getEditForm(
		                    @RequestParam(value="__node", required=true) String node, 
												@RequestParam(value="__id", required=true) String id,
												@RequestParam(value="__params", required=false) String params){
		try {
			PageContext context = new PageContext(node, id, meta, navmodel, urlfactory, data);
			return new JSONResponse(getForm(context, node, context.Class.getName(), params));
    } catch (IonException e) {
	      return new JSONResponse(e.getMessage());
    }
	}
	
	private void setReadOnly(IField f) {
		if (f instanceof Field)
			((Field)f).setReadonly(true);		
		if (f instanceof FieldGroup)
			for (IField sf: ((FieldGroup) f).getFields())
				setReadOnly(sf);
	}	

	
	@RequestMapping(value="/spa/edit/vm", 
			method = { RequestMethod.POST, RequestMethod.HEAD },
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse getEditVm(
	                    @RequestParam(value="__node", required=true) String node, 
	                    @RequestParam(value="__id", required=true) String id,
											@RequestParam(value="__class", required=false) String className){
		try {
			PageContext context = new PageContext(node, id, meta, navmodel,urlfactory, data);
			String cc = (className == null)?context.Class.getName():className;
			//data.initItem(item, true);

			IStructMeta cm = meta.Get(cc);
			IItem item = data.getItem(cm.getName(), context.Id);
			if (item != null)
				data.initItem(item, true);			

			return new JSONResponse(getFormView(node, cm, item));
		} catch (IonException e) {
			e.printStackTrace();
			return new JSONResponse(e.getMessage());
		}
	}
	
	@RequestMapping(value="/spa/edit",
			method = { RequestMethod.POST, RequestMethod.HEAD }, 
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody ActionResponse update(
	                       MultipartHttpServletRequest request, 
	                       Model model, 
	                       @RequestParam(value="__node", required=true) String node, 
	                       @RequestParam(value="__action", required=true) String form_action_name
	                     ) throws IonException, ParseException, IOException{
		return handleAction(node, request, model, form_action_name);
	}	
	
	@RequestMapping(value="/spa/editItem",
			method = { RequestMethod.POST, RequestMethod.HEAD },
			headers = "x-requested-with=XMLHttpRequest",
			produces = "application/json;charset=UTF-8")
	public @ResponseBody JSONResponse updateItem(MultipartHttpServletRequest request) {
		try {
			String requestedItemInfo = request.getParameter("itemInfo");
			if(requestedItemInfo != null){
				String[] itemInfo = requestedItemInfo.split(",");
				String className = itemInfo[0];
				String id = itemInfo[1];
		    IItem item = data.Edit(className, id, postedData(className,LocaleContextHolder.getLocale(),request), 3);
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
		if (actionName.equals(ActionType.EDIT.toString()) || actionName.equals(ActionType.SAVE.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						IItem item = data.Edit(context.Class.getName(), context.Id, postedData(context.Class.getName(),LocaleContextHolder.getLocale(),request),3);
						//String redirect = "/item?__node="+Urls.parseNodeId(context.getNode().getId()) + "&__id=" + item.getClassName() + ":" + item.getItemId();
						return new ActionResponse(data.ItemToMap(item), null, new IonMessage("Данные сохранены", IonMessageType.INFO));
					} catch (ParseException e) {
						throw new IonException(e);
					}
				}
			};
		if (actionName.equals(ActionType.DELETE.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						IItem item = data.getItem(context.Class.getName(), context.Id);
						final String id = item.getItemId();
						data.Delete(item.getClassName(),id);
						String redirect = "/list?__node="+Urls.parseNodeId(context.getNode().getId());
						return  new ActionResponse(null,redirect);
					} catch (Exception e) {
						throw new IonException(e);
					}
				}
			};
		
		return super.getDefaultHandler(actionName, session);
	}

	@Override
	protected IFormViewModel getFormView(String node, IStructMeta cm, IItem item) throws IonException {
		IFormViewModel vm = viewmodel.getItemViewModel(node, cm);
    if (vm == null)
    	vm = viewmodel.getItemViewModel(cm);
    
		HashSet<ActionType> defActions = new HashSet<ActionType>();
		
		boolean readOnly = true;
		
		if (data.CheckClassSavingAccess(cm.getName())){
			defActions.add(ActionType.SAVE);
			defActions.add(ActionType.CANCEL);
			readOnly = false;
		} else {
			vm.getActions().remove(ActionType.SAVE.name());
		}	    
    
		if (vm == null)
			vm = defaultViewModel((IClassMeta)cm, defActions);

		if (vm.getMode() != null){
			if(vm.getMode() == ViewApplyMode.OVERRIDE){
				IFormViewModel vmToOverride = defaultViewModel((IClassMeta)cm, defActions);
				vm = createOverrideModel(vm, vmToOverride);
			}
		}
		
		if (item != null){
			IWorkflowState wfState = data.GetWorkflowState(item);
			Collection<String> readonlyProps = new ArrayList<String>();
			Collection<String> deniedProps = new ArrayList<String>();

			if(wfState != null){
				IWorkflowTransition[] wfTransitions = wfState.getNextTransitions(authContext.CurrentUser());
				for(IWorkflowTransition transition : wfTransitions){
					vm.getActions().put(transition.getActionName(), new FormAction(transition.getActionName(), transition.getCaption(), null, null, transition.getSignBefore(), transition.getSignAfter()));
				}
				
				for(String property : item.getProperties().keySet()){
					if(!wfState.checkPropertyPermission(property, authContext.CurrentUser(), DACPermission.WRITE)){
						if(wfState.checkPropertyPermission(property, authContext.CurrentUser(), DACPermission.READ)){
							readonlyProps.add(property);
						} else {
							deniedProps.add(property);
						}
					}
				}
			}
			
			if (readOnly || !readonlyProps.isEmpty() || !deniedProps.isEmpty()){
  			for(IFormTab tab : vm.getTabs()){
  				Collection<IField> fieldsToRemove = new HashSet<IField>();
  				
  				for(IField field : tab.getFullViewFields()){
  					if(deniedProps.contains(field.getProperty())) {
  						fieldsToRemove.add(field);
  					} else if (readOnly || readonlyProps.contains(field.getProperty())){
  						setReadOnly((Field)field);
  					} 
  				}
  				tab.getFullViewFields().removeAll(fieldsToRemove);
  				fieldsToRemove.clear();
  				
  				for(IField field : tab.getShortViewFields()){
  					if(deniedProps.contains(field.getProperty())){
  						fieldsToRemove.add(field);
  					} else if (readOnly || readonlyProps.contains(field.getProperty())){
  						setReadOnly((Field)field);
  					}
  				}
  				tab.getShortViewFields().removeAll(fieldsToRemove);
  			}
			}
		}
		return vm;
	}
}
