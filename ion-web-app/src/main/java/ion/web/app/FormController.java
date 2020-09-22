package ion.web.app;

import ion.core.IClassMeta;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IUserTypePropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.meta.ReferencePropertyMeta;
import ion.viewmodel.view.FormAction;
import ion.viewmodel.view.FormTab;
import ion.viewmodel.view.FormTab.TabMode;
import ion.viewmodel.view.IField;
import ion.viewmodel.view.IFormTab;
import ion.viewmodel.view.IFormViewModel;
import ion.web.app.util.JSONResponse;
import ion.web.app.util.PageContext;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public abstract class FormController extends BasicController {
	
	protected String formatValue(MetaPropertyType t, Object v){
		if (v == null)
			return "";
		if (t == MetaPropertyType.DATETIME)
			return FormatDate((Date)v);
		return v.toString();
	}	
	
	protected Object parsePostedValue(IPropertyMeta pm, String value, Locale locale) throws ParseException, IonException{
		if (value != null){
			if (value.trim().length() == 0)
				return null;

			MetaPropertyType type = pm.Type();
			if (type == MetaPropertyType.CUSTOM) {
				type = ((IUserTypePropertyMeta)pm).UserType().getBaseType();
			}
			
			switch (type){
				case REFERENCE:{
					IClassMeta rc = ((ReferencePropertyMeta)pm).ReferencedClass();
					return parsePostedValue(rc.PropertyMeta(rc.KeyProperty()),value,locale); 
				}
				case BOOLEAN:return Boolean.parseBoolean(value.equals("on")?"true":value);
				case DATETIME:return DateFormat.getDateInstance(DateFormat.SHORT, locale).parse(value);
				default:return value;
			}
		}	
		return value;
	}
		
	protected Map<String, Object> postedData(String classname, Locale locale, MultipartHttpServletRequest request) throws IonException, ParseException{
		Map<String, Object> result = new HashMap<String, Object>();
		IStructMeta cm = meta.Get(classname);
		IPropertyMeta pm = null;
		String v;
		String nm;
		String fn;
		for (
		Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();){
			nm = names.nextElement();
			v = request.getParameter(nm);
			pm = cm.PropertyMeta(nm);
			if (pm != null)
			switch (pm.Type()){
				case IMAGE:
				case FILE:{
					MultipartFile f = request.getFile(nm);
					try {
						result.put(nm,filestorage.Accept(f.getInputStream(), f.getOriginalFilename()));
					} catch (IOException e) {
						throw new IonException("Ошибка сохранения файла!", e);
					}
				}break;
				case SET:{
					/**
					 * @TODO: Определиться со способом редактирования множеств
					 */
				}break;
				case COLLECTION:{
					
				}break;
				default:result.put(nm,parsePostedValue(pm, v, locale));break;
			}
		}
		
		for(Iterator<String> files = request.getFileNames(); files.hasNext();){
			fn = files.next();
			pm = cm.PropertyMeta(fn);
			if (pm!=null){
				switch (pm.Type()){
					case IMAGE:
					case FILE:{
						MultipartFile f = request.getFile(fn);
						if(f!=null && f.getOriginalFilename()!=""){
							try {
								result.put(fn,filestorage.Accept(f.getInputStream(), f.getOriginalFilename()));
							} catch (IOException e) {
								throw new IonException("Ошибка сохранения файла!", e);
							}	
						}
					}break;
					default:
						break;
				}
			}
		}
		return result;
	}
	
	protected Map<String, Object> postedFile(String classname, Locale locale, MultipartHttpServletRequest request) throws IonException, ParseException{
		Map<String, Object> result = new HashMap<String, Object>();
		IStructMeta cm = meta.Get(classname);
		IPropertyMeta pm = null;
		String fn;
		Iterator<String> files = request.getFileNames();
		if(files.hasNext()){
			fn = files.next();
			pm = cm.PropertyMeta(fn);
			if (pm!=null){
				switch (pm.Type()){
					case IMAGE:
					case FILE:{
						MultipartFile f = request.getFile(fn);
						if(f!=null && f.getOriginalFilename()!=""){
							try {
								result.put(fn,filestorage.Accept(f.getInputStream(), f.getOriginalFilename()));
							} catch (IOException e) {
								throw new IonException("Ошибка сохранения файла!");
							}	
						}
					}break;
					default:
						break;
				}
			}
		}
		return result;
	}
	
	protected IFormViewModel createOverrideModel(IFormViewModel vm, IFormViewModel vmToOverride){
		
		ArrayList<IField> resultFullFields = new ArrayList<IField>();
		
		for(IFormTab ft : vmToOverride.getTabs()){
			resultFullFields.addAll(ft.getFullViewFields());
		}
		
		FormTab tab = (FormTab)vm.getTabs().toArray(new IFormTab[vm.getTabs().size()])[0];
		ArrayList<IField> flat = new ArrayList<IField>();
		flattenFields(flat, tab.getFullViewFields());
		resultFullFields.removeAll(flat);
		resultFullFields.addAll(tab.getFullViewFields());
		tab.getFullViewFields().clear();
		tab.addFields(resultFullFields,TabMode.FULLVIEW);
		
		Map<String, FormAction> actions = vm.getActions();
		for(Entry<String, FormAction> entry: vmToOverride.getActions().entrySet()){
			if(!actions.containsKey(entry.getKey())){
				actions.put(entry.getKey(), entry.getValue());
			}
		}
		return vm;
	}
	
	protected IActionHandler getDefaultHandler(String actionName, HttpSession session){
		return null;
	}
	
	protected Object execAction(String actionName, PageContext context, MultipartHttpServletRequest request, HttpSession session) throws IonException {
		IActionHandler handler = null;
	    if (applicationContext.containsBean(actionName)) {
    	    Object bean = applicationContext.getBean(actionName);
    	    if (bean instanceof IActionHandler)
    	        handler = (IActionHandler)bean;
	    } 
	    
	    if (handler == null)
	    	handler = getDefaultHandler(actionName,session);
	    
	    if (handler != null)
	    	return handler.executeAction(context, request);
	    
	    throw new IonException("Не найден обработчик для данного действия!");
	}
	
    public JSONResponse handleAjax(String node, MultipartHttpServletRequest request, HttpSession session, Model model, String form_action_name) throws IonException, ParseException, IOException {
    	try {
    		PageContext context = null;
    		if (request.getParameter("id") != null)
    			context = new PageContext(node, request.getParameter("id"), meta, navmodel, urlfactory, data);
    		else
    			context = new PageContext(node, meta, navmodel, urlfactory, data);
    		Object result = execAction(form_action_name, context, request, session);
    		if (result instanceof JSONResponse)
    			return (JSONResponse)result;
    		return new JSONResponse(result);
		} catch (IonException e){
			model.asMap().clear();
			return new JSONResponse(e.getMessage());
		}    		
    }	
}
