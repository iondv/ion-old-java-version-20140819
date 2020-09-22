package ion.web.app;

import ion.core.IClassMeta;
import ion.core.IItem;
import ion.core.IonException;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.ViewApplyMode;
import ion.web.app.FormController;
import ion.web.app.jstl.Urls;
import ion.web.app.util.INavigationNode;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.app.util.PageContext;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.servlet.http.HttpSession;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class AbstractEditController extends FormController {
	
	protected void setupForm(String node, String id, Model model) throws IonException {
		PageContext context = new PageContext(node, id, meta, navmodel,urlfactory, data);
		model.addAttribute("context",context);
		model.addAttribute("breadcrumbs",context.BreadCrumbs(data));
		IItem item = data.getItem(context.Class.getName(), context.Id);
		//data.initItem(item, true);
		model.addAttribute("Title","ION: "+item.toString());
		model.addAttribute("item",item);
		IFormViewModel vm = viewmodel.getItemViewModel(node, item.getMetaClass());

        @SuppressWarnings("serial")
        HashSet<ActionType> defActions = new LinkedHashSet<ActionType>(){{ add(ActionType.SAVE); add(ActionType.DELETE);}};

        if (vm == null)
			vm = viewmodel.getItemViewModel(item.getMetaClass());
			
		if (vm == null)
			vm = defaultViewModel((IClassMeta)item.getMetaClass(), defActions);

		if (vm.getMode() != null){
			if(vm.getMode() == ViewApplyMode.OVERRIDE){
				IFormViewModel vmToOverride = defaultViewModel((IClassMeta)item.getMetaClass(), defActions);
				vm = createOverrideModel(vm, vmToOverride);
			}
		}
		
		model.addAttribute("viewmodel", vm);
		model.addAttribute("validators", viewmodel.getValidators());
		Urls.storage = filestorage;
		
		String formFile = customJspExist(node,context.Class.getName(),"form");
		if(formFile!= null){
			formFile = "../"+formFile+".jsp";
		} else {
			formFile = "form.jsp";
		}
		model.addAttribute("inputForm", formFile);
	}
	
	public String form(String node, String id, Model model) {
		try {
			setupForm(node,id,model);
		} catch (IonException e){
			handleException(e, model);
		}
		return ThemeDir()+"edit";
	}
	
	protected IActionHandler getDefaultHandler(String actionName, final HttpSession session){
		if (actionName.equals(ActionType.EDIT.toString()) || actionName.equals(ActionType.SAVE.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						IItem item = data.Edit(context.Class.getName(), context.Id, postedData(context.Class.getName(),LocaleContextHolder.getLocale(),request));						
						if (request.getHeader("x-requested-with") != null 
								&& request.getHeader("x-requested-with").equals("XMLHttpRequest"))
							return item;
						session.setAttribute("IonMessage", new IonMessage("Действие выполнено успешно", IonMessageType.INFO));
						return "redirect:"+context.getLink(item);
					} catch (ParseException e) {
						throw new IonException(e);
					}
				}
			};
		if (actionName.equals(ActionType.DELETE.toString()))
			return new IActionHandler() {
				@SuppressWarnings("serial")
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						IItem item = data.getItem(context.Class.getName(), context.Id);
						INavigationNode pp = context.ParentPage(item);					
						final String id = item.getItemId();
						data.Delete(item.getClassName(),id);
						if (request.getHeader("x-requested-with") != null 
								&& request.getHeader("x-requested-with").equals("XMLHttpRequest"))
							return new HashMap<String, String>(){{put("deleted",id);}};
						if (pp != null)	
							return "redirect:"+pp.getUrl();
						return "redirect:"+context.getNode().getUrl();
					} catch (Exception e) {
						throw new IonException(e);
					}
				}
			};
		
		return super.getDefaultHandler(actionName,session);
	}
    
    public String handle(String node, String id, MultipartHttpServletRequest request, HttpSession session, Model model, String form_action_name) throws ParseException, IOException, IonException {
    	try {
    		PageContext context = new PageContext(node, id, meta, navmodel, urlfactory, data);
	    	Object result = execAction(form_action_name, context, request, session);
	    	model.asMap().clear();
	        return result.toString();
		} catch (IonException e) {
			handleException(e, model);
		}
		return form(node, id, model);        
    }	
}
