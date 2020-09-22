package ion.web.app;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpSession;

import ion.core.IItem;
import ion.core.IonException;
import ion.viewmodel.view.ActionType;
import ion.web.app.BasicCreateController;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.app.util.PageContext;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartHttpServletRequest;


public class AbstractCreateController extends BasicCreateController {
	
	public String form(String node, String cc, Model model) throws ParseException {
		try {
			setupForm(node,cc,null,null,model);
		} catch (IonException e){
			handleException(e, model);
		}
		return ThemeDir()+"create";
	}
	
	protected IActionHandler getDefaultHandler(String actionName, final HttpSession session){
		if (actionName.equals(ActionType.CREATE.toString()) || actionName.equals(ActionType.SAVE.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						String cc = request.getParameter("cc");
						if (cc == null)
							cc = context.Class.getName();
						IItem item = data.Create(cc, postedData(cc, LocaleContextHolder.getLocale(), request));
						session.setAttribute("IonMessage", new IonMessage("Действие выполнено успешно", IonMessageType.INFO));
						if (request.getHeader("x-requested-with") != null 
								&& request.getHeader("x-requested-with").equals("XMLHttpRequest"))
							return item;
						return "redirect:" + context.getLink(item);
					} catch (ParseException e) {
						throw new IonException(e);
					}
				}
			};
			
		if (actionName.equals(ActionType.CANCEL.toString()))
			return new IActionHandler() {
				@Override
				public Object executeAction(PageContext context,
						MultipartHttpServletRequest request) throws IonException {
					try {
						return "redirect:"+context.getLink();
					} catch (Exception e) {
						throw new IonException(e);
					}
				}
			};			
		
		return super.getDefaultHandler(actionName,session);
	}
    
    public String handle(String node, MultipartHttpServletRequest request, HttpSession session, Model model, String form_action_name) throws ParseException, IOException, IonException {
    	PageContext context = new PageContext(node, meta, navmodel, urlfactory, data);
    	try {
	    	Object result = execAction(form_action_name,context, request, session);
	    	model.asMap().clear();
	    	if (result != null)
	    		return result.toString();
		} catch (IonException e) {
			handleException(e, model);
		}
		return form(node, (request.getParameter("cc") != null)?request.getParameter("cc"):context.Class.getName(),model);        
    }
}