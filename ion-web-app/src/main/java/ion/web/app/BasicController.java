package ion.web.app;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import ion.core.IClassMeta;
import ion.core.IMetaRepository;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IUserContext;
import ion.core.IUserTypeMeta;
import ion.core.IUserTypePropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.digisign.IDigiSignDataProvider;
import ion.core.meta.ReferencePropertyMeta;
import ion.core.meta.UserTypePropertyMeta;
import ion.core.storage.IFileStorage;
import ion.viewmodel.navigation.INavigationModel;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.CollectionField;
import ion.viewmodel.view.CollectionFieldMode;
import ion.viewmodel.view.DataField;
import ion.viewmodel.view.FieldGroup;
import ion.viewmodel.view.FieldSize;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.FormTab;
import ion.viewmodel.view.FormViewModel;
import ion.viewmodel.view.IField;
import ion.viewmodel.view.IFieldGroup;
import ion.viewmodel.view.IFormTab;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.IListColumn;
import ion.viewmodel.view.IViewModelRepository;
import ion.viewmodel.view.ReferenceField;
import ion.viewmodel.view.ReferenceFieldMode;
import ion.viewmodel.view.Validator;
import ion.core.IAuthContext;
import ion.db.search.IFulltextSearchAdapter;
import ion.web.app.service.IonWebAppService;
import ion.web.app.util.INavigationNode;
import ion.web.app.util.INavigationProvider;
import ion.web.app.util.IonMessage;
import ion.web.app.util.IonMessageType;
import ion.web.util.com.AppList;
import ion.web.util.com.WebAppLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;


public class BasicController {
	
	@Autowired(required=false)
	public WebAppLogger logger;
	
	@Autowired
	public ServletContext servletContext;
	
	@Autowired
	protected IAuthContext authContext;	
	
	@Autowired
	protected INavigationProvider navigation;
	
	@Autowired
	protected IMetaRepository meta;
	
	@Autowired
	protected IonWebAppService data;
	
	@Autowired
	protected INavigationModel navmodel;
	
	@Autowired
	protected IViewModelRepository viewmodel;

	@Autowired
	protected IFileStorage filestorage;
	
	@Autowired
	protected UrlFactory urlfactory;
	
	@Autowired
	protected AppList applist;
	
	@Autowired(required=false)
	protected SectionContextHolder section;
	
	@Autowired
	protected ApplicationContext applicationContext;
		
    @Value("${app.defaultTitle}")
    private String applicationTitle;
    
    @Value("${app.themeDir}")
    private String themeDir;
    
    
    protected String formTitle(){
    	return applicationTitle;
    }
    
    @ModelAttribute("fullTextSearchAvailable")
    public boolean fullTextSearchAvailable(){
    	return applicationContext.getBeanNamesForType(IFulltextSearchAdapter.class).length > 0;
    }
    
    
    @ModelAttribute("digtalSigningAvailable")
    public boolean isDigitalSigningAvailable(){
    	return applicationContext.getBeanNamesForType(IDigiSignDataProvider.class).length > 0;
    }    
    
    @ModelAttribute("ThemeDir")
    public String ThemeDir(){
    	return (themeDir == null)?"":(themeDir+"/");
    }
    
    @ModelAttribute("currentSection")
    public String CurrentSection(){
    	if (section != null)
    		return section.getSection();
    	return "";
    }    
    
    public String customJspExist(String node, String className, String type){
    	String path = "/WEB-INF/views/ionweb-custom/"+"/"+node.replaceAll("[^0-9a-zA-Z]+", "/")+"/"+className+"_"+type+".jsp";
    	String realPath = servletContext.getRealPath(path);
		Boolean exist = false;
    	if (realPath  == null){
    		ServletContextResource r = new ServletContextResource(servletContext,path);
    		try {
				exist = r.getFile().exists();
			} catch (IOException e) {
				e.printStackTrace();
			}

    	} else {
    		FileSystemResource r = new FileSystemResource(path);
    		exist = r.getFile().exists(); 
    	}
    	if(exist) return "ionweb-custom/"+"/"+node.replaceAll("[^0-9a-zA-Z]+", "/")+"/"+className+"_"+type;
		return null;
    }
    
    @ModelAttribute("AppLinks")
    public Map<String, String> AppLinks(){
    	return applist.getList();
    }    
    
	@ModelAttribute("Title")
	public String getTitle(Model model) {
		return formTitle();
	}
	
	@ModelAttribute("User")
	public String getUser(Model model) {
		if (authContext.CurrentUser() != null)
			return authContext.CurrentUser().toString();
		return null;
	}
	
	@ModelAttribute("UserContext")
	public IUserContext getUserContext(Model model) {
		if (authContext.CurrentUser() != null)
			return authContext.CurrentUser();
		return null;
	}		
	
	@ModelAttribute("Validators")
	public Map<String, Validator> getValidators(Model model) {
		if (viewmodel.getValidators() != null)
			return viewmodel.getValidators();
		return null;
	}	
	
	private String getCauseMessage(Throwable e){
		/*if (e instanceof HibernateException){
			return ((HibernateException)e).getMessage();
		}*/
		if (e.getCause() != null)
			return getCauseMessage(e.getCause());
		
		return e.getLocalizedMessage();
	}
	
	protected void showMessage(IonMessage message,Model model){
		model.addAttribute("MessageOccured", true);
		model.addAttribute("IonMessageType", message.getType().toString());
		model.addAttribute("IonMessageText", message.getMessage());
	}
	
	public void getMessageFromSession(HttpSession session, Model model){
		Object message = session.getAttribute("IonMessage");
		if(message !=null){
			showMessage((IonMessage)message, model);
			session.removeAttribute("IonMessage");
		}
	}
		
	protected void handleException(IonException e, Model model){
		showMessage(new IonMessage(getCauseMessage(e), IonMessageType.ERROR), model);
		if (logger != null)
			logger.Error(e.getMessage(), e);
	}
	
	protected void flattenFields(ArrayList<IField> result, Collection<IField> src){
		for (IField f : src){
			if (f instanceof FieldGroup)
				flattenFields(result, ((FieldGroup) f).getFields());
			else
				result.add(f);
		}
	}	
		
	@ModelAttribute("DateFormat")
	public String getDateFormat(Model model){
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, LocaleContextHolder.getLocale());
		if (df instanceof SimpleDateFormat){
			return ((SimpleDateFormat) df).toPattern();
		}
		return "";
	}	
	
	private void checkNavAccess(Collection<INavigationNode> src) throws IonException{
		Collection<INavigationNode> tmp = new LinkedList<INavigationNode>();
		tmp.addAll(src);
		for (INavigationNode n: tmp) {
			checkNavAccess(n.getNodes());
			if (!(n.getNodes().size() > 0 || data.CheckNavAccess(n.getId())))
				src.remove(n);
		}
	}
			
	@ModelAttribute("Menu")
	public Collection<INavigationNode> menu(Model model) throws IonException { 
		Collection<INavigationNode> result = navigation.RootNodes("MENU");
		checkNavAccess(result);
		return result;
	}
		
	protected String FormatDate(Date v){
		return DateFormat.getDateInstance(DateFormat.SHORT, LocaleContextHolder.getLocale()).format(v);
	}
	
	protected FieldSize fieldSize(Short size){
		if (size == null)
			return FieldSize.MEDIUM;
		if (size < 4)
			return FieldSize.TINY;
		if (size < 10)
			return FieldSize.SHORT;
		if (size < 20)
			return FieldSize.MEDIUM;
		if (size < 40)
			return FieldSize.LONG;
		return FieldSize.BIG;
	}
	
	protected FieldType defaultFieldType(IPropertyMeta pm){
		return FieldType.fromPropertyType(pm.Type());
	}	
	
	protected final IFieldGroup formFieldGroup(IStructPropertyMeta pm, Set<FieldType> ignoreFieldTypes) throws IonException{
		ArrayList<IField> group = new ArrayList<IField>();
		IStructMeta m = pm.StructClass();
		for(IPropertyMeta gpm : m.PropertyMetas().values())
			if (!ignoreFieldTypes.contains(FieldType.fromPropertyType(gpm.Type()))){
			if (gpm.Type().equals(MetaPropertyType.REFERENCE))
				group.add(new ReferenceField(gpm.Caption(),pm.Name() + "$" + gpm.Name(), ReferenceFieldMode.LINK, new ArrayList<IField>(), gpm.OrderNumber(), !gpm.Nullable(), pm.ReadOnly()?true:gpm.ReadOnly(),new HashSet<ActionType>()));
			else if (gpm.Type().equals(MetaPropertyType.COLLECTION))
				group.add(new CollectionField(gpm.Caption(),pm.Name() + "$" + gpm.Name(), CollectionFieldMode.LINK, new ArrayList<IListColumn>(), new HashSet<ActionType>(), gpm.OrderNumber(),pm.ReadOnly()?true:gpm.ReadOnly()));
			else if (gpm.Type().equals(MetaPropertyType.STRUCT))
				group.add(formFieldGroup((IStructPropertyMeta)gpm, ignoreFieldTypes));
			else if (gpm.Type().equals(MetaPropertyType.CUSTOM)){
				IUserTypeMeta utm = ((UserTypePropertyMeta)gpm).UserType();
				group.add(new DataField(gpm.Caption(), pm.Name() + "$" + gpm.Name(), fieldSize(gpm.Size()), utm.getMaskName(), FieldType.fromPropertyType(utm.getBaseType()), gpm.OrderNumber(), !gpm.Nullable(), utm.getMask(),gpm.ReadOnly()?true:gpm.ReadOnly()));
			} else	
				group.add(new DataField(gpm.Caption(), pm.Name() + "$" + gpm.Name(), defaultFieldType(gpm), fieldSize(gpm.Size()), gpm.OrderNumber(), !gpm.Nullable(), gpm.ReadOnly()?true:gpm.ReadOnly()));
		}
		
		return new FieldGroup(pm.Caption(),group, pm.Name(), pm.OrderNumber(),pm.ReadOnly());
	}	
	
	@SuppressWarnings("serial")
	protected IFormViewModel defaultViewModel(IStructMeta meta, Set<ActionType> defaultActions, Set<FieldType> ignoreFieldTypes) throws IonException{
		ArrayList<IField> fields = new ArrayList<IField>();
		
		for(IPropertyMeta pm : meta.PropertyMetas().values()){
			if(!ignoreFieldTypes.contains(FieldType.fromPropertyType(pm.Type()))){
  			if (meta instanceof IClassMeta)
  				if (pm.Name().equals(((IClassMeta)meta).KeyProperty()) && pm.AutoAssigned())
  					continue;
  			
  			if (pm.Type() == MetaPropertyType.REFERENCE)
  				fields.add(new ReferenceField(pm.Caption(), pm.Name(), true, ReferenceFieldMode.LINK, new ArrayList<IField>(), pm.OrderNumber(), !pm.Nullable(), pm.ReadOnly(),new HashSet<ActionType>()));
  			else if (pm.Type() == MetaPropertyType.COLLECTION)
  				fields.add(new CollectionField(pm.Caption(), pm.Name(), CollectionFieldMode.LINK, new ArrayList<IListColumn>(), new HashSet<ActionType>(), pm.OrderNumber(), pm.ReadOnly()));
  			else if (pm.Type() == MetaPropertyType.STRUCT)
  				fields.add(formFieldGroup((IStructPropertyMeta)pm, ignoreFieldTypes));
  			else if (pm.Type() == MetaPropertyType.CUSTOM) {
  				IUserTypeMeta utm = ((UserTypePropertyMeta)pm).UserType();
  				fields.add(new DataField(pm.Caption(), pm.Name(), fieldSize(pm.Size()), utm.getMaskName(), FieldType.fromPropertyType(utm.getBaseType()), pm.OrderNumber(), !pm.Nullable(), utm.getMask(), pm.ReadOnly()));
  			} else {
  				fields.add(new DataField(pm.Caption(), pm.Name(), defaultFieldType(pm), fieldSize(pm.Size()), pm.OrderNumber(), !pm.Nullable(), pm.ReadOnly()));
  			}
			}
		}
		
		final FormTab tab = new FormTab("",fields);		
		return new FormViewModel(new ArrayList<IFormTab>(){{ add(tab);}}, defaultActions, null);		
	}
	
	protected IFormViewModel defaultViewModel(IStructMeta meta, Set<ActionType> defaultActions) throws IonException{
		return defaultViewModel(meta, defaultActions, new HashSet<FieldType>());
	}
	
	protected IFormViewModel defaultViewModel(IStructMeta meta) throws IonException{
		return defaultViewModel(meta, new HashSet<ActionType>());
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
						throw new IonException("Ошибка сохранения файла!");
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
	
	protected Object parsePostedValue(IPropertyMeta pm, String value, Locale locale) throws ParseException, IonException{
		if (value != null){
			if (value.trim().length() == 0)
				return null;

			MetaPropertyType type = pm.Type();
			if (type == MetaPropertyType.USER) {
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
	
}
