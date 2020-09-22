package ion.offline.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.logging.ILogger;
import ion.core.logging.IonLogger;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSelectionProvider;
import ion.integration.core.UserCredentials;
import ion.offline.adapters.SitexSoapRequestor;
import ion.offline.net.ActionResult;
import ion.offline.net.AuthResult;
import ion.offline.net.DataChange;
import ion.offline.net.DataChangeType;
import ion.offline.net.DataDelta;
import ion.offline.net.DataUnit;
import ion.offline.net.MetaDelta;
import ion.offline.net.NavigationDelta;
import ion.offline.net.UserProfile;
import ion.offline.net.ViewDelta;
import ion.offline.security.HashProvider;
import ion.offline.util.IHashProvider;
import ion.offline.util.ITargetSystemAdapter;
import ion.offline.util.SyncDelta;
import ion.viewmodel.plain.StoredAction;
import ion.viewmodel.plain.StoredColumn;
import ion.viewmodel.plain.StoredField;
import ion.viewmodel.plain.StoredFormViewModel;
import ion.viewmodel.plain.StoredListViewModel;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredTab;
import ion.viewmodel.plain.StoredViewModel;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.CollectionFieldMode;
import ion.viewmodel.view.FieldSize;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.HistoryDisplayMode;
import ion.viewmodel.view.ViewApplyMode;

public class SitexServicePortalAdapter implements ITargetSystemAdapter {
	
	private String hashDir;

	private String viewsDir;
	
	private String digestTemplateHashDir;
	
	private boolean loadDictionaries = false;
	
	private Set<String> dictionaryExcludeList = new HashSet<String>();
	
	private Set<String> dictionaryIncludeList = new HashSet<String>();
	
	private Set<String> digTplExcludeList = new HashSet<String>();
	
	private Set<String> digTplIncludeList = new HashSet<String>();

	private Map<String, Set<String>> _dictionaries = new HashMap<String, Set<String>>();
	
	private Map<String, Map<String,String>> _collectionLinkClasses = new HashMap<String, Map<String,String>>();
	
	private IHashProvider hasher;

	private ISitexSoapRequestor requestor;
	
	private Properties classTypeTitles;	
	
	private IonLogger logger;
	
	private boolean debug = false;
	
	private String digestTplEncoding = "utf-8";
	
	private Map<String, Map<String, String>> classAttrTabs = new TreeMap<String, Map<String, String>>();	
	
	private Map<String, Map<String, String>> classAttrGroups = new TreeMap<String, Map<String, String>>();
	
	private Map<String, Map<String, Collection<String>>> classCollectionColumns = new TreeMap<String, Map<String,Collection<String>>>();
	
	private List<String> initDictionaries = new LinkedList<String>();
	
	public static final String			 CN_FEDERAL		= "FederalService";

	public static final String			 CN_REGIONAL	 = "RegionalService";

	public static final String			 CN_PETITION	 = "Petition";
	
	public static final String			 CN_ROOT			 = "SmevEntity";

	public SitexServicePortalAdapter(int ver) throws IonException {
		if (ver == 0)
			requestor = new SitexSoapRequestor();
		else
			requestor = new SitexSoapRequestor2();
		hasher = new HashProvider();
		logger = new IonLogger("Sitex PPU portal adapter");
	}
	
	public AuthResult[] authenticate(UserCredentials[] credentials)
			throws IonException {
		return requestor.authenticate(credentials);
	}
	
	private void clearParentProps(StoredClassMeta cm, Map<String, StoredClassMeta> classes){
		if (cm.ancestor != null && !cm.ancestor.isEmpty()){
			if (classes.containsKey(cm.ancestor)){
				StoredClassMeta anc = classes.get(cm.ancestor);
				for (StoredPropertyMeta apm: anc.properties){
					List<StoredPropertyMeta> tmp = new ArrayList<StoredPropertyMeta>(cm.properties);
					for (StoredPropertyMeta pm: tmp){
						if (pm.name.equals(apm.name))
							cm.properties.remove(pm);
					}
					tmp.clear();
				}
			}
		}
	}
	
	private void propsDiff(StoredClassMeta c1, StoredClassMeta c2, boolean invert){
		if (c1 == null || c2 == null)
			return;
		
		List<StoredPropertyMeta> tmp = new LinkedList<StoredPropertyMeta>();
		Set<String> search = new HashSet<String>();
		
		for (StoredPropertyMeta pm: c2.properties)
			search.add(pm.name);
		
		tmp.addAll(c1.properties);
		
		for (StoredPropertyMeta apm: tmp){
			if (invert ^ !search.contains(apm.name))
				c1.properties.remove(apm);
		}
	}
	
	private void formProfiles(String client, String[] users, Date since, List<UserProfile> profiles, Set<String> classNames, Map<String, Set<String>> classOrgs, Map<String, String> classTypes, Map<String, String[]> classNav) throws IonException{
		for (String u: users){
			UserProfile p = requestor.getProfile(u, null, classNames, classOrgs, classTypes, classNav);
			if (p != null)
				profiles.add(p);
					//logger.Info("Profile of user "+ u + " added to package");
		}
	}
	
  @SuppressWarnings({ "unchecked", "rawtypes" })
	private void formClassDelta(String client, /*Date since,*/ Set<String> classNames, List<UserProfile> profiles,
                              Map<String, Set<String>> classOrgs, Map<String, String> classTypes, 
                              Map<String, String[]> classNav, Map<String, StoredClassMeta> classes, 
                              Map<String, String> classWsdl, List<StoredClassMeta> classesDelta, 
                              List<StoredNavNode> changedNav, Map<String, StoredViewModel> changedViews,
                              Map<String, Integer> dictPermissions) throws IonException{
		
  	requestor.requestClasses(classNames, /*since, */classes, classAttrTabs, classAttrGroups, classCollectionColumns,
  	                         classWsdl, dictPermissions, _dictionaries, classTypes, profiles, _collectionLinkClasses);
  	
		for (StoredClassMeta c: classes.values()){
			clearParentProps(c, classes);
		}
		
		StoredClassMeta root = classes.get(SitexServicePortalAdapter.CN_ROOT);
		StoredClassMeta fed = classes.get(SitexServicePortalAdapter.CN_FEDERAL);
		StoredClassMeta reg = classes.get(SitexServicePortalAdapter.CN_REGIONAL);
		StoredClassMeta pet = classes.get(SitexServicePortalAdapter.CN_PETITION);
		
		if (root != null){
  		if (fed != null)
  			root.properties.addAll(fed.properties);
  		else if (reg != null)
  			root.properties.addAll(reg.properties);
  		else if (pet != null)
  			root.properties.addAll(pet.properties);
  		
  		propsDiff(root, fed, false);		
  		propsDiff(root, reg, false);
  		propsDiff(root, pet, false);
  		
  		propsDiff(fed, root, true);
  		propsDiff(reg, root, true);
  		propsDiff(pet, root, true);
  		
  		root.properties.add(new StoredPropertyMeta(0,
  		                                           "guid",
  																								"Глобальный идентификатор",
  																								MetaPropertyType.GUID.getValue(),
  																								null,null,
  																								false,true,false,false,true,
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); //guid
  		root.properties.add(new StoredPropertyMeta(1,
  		                                           "ouid",
  																								"Номер",
  																								MetaPropertyType.INT.getValue(),
  																								null,null,
  																								true,true,false,false,false,
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); //ouid
  		root.properties.add(new StoredPropertyMeta(2,
  		                                           "timeStamp",
  																								"Изменен",
  																								MetaPropertyType.DATETIME.getValue(),
  																								null,null,
  																								true,true,false,false,false,
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); //timeStamp
  		root.properties.add(new StoredPropertyMeta(3,
  		                                           "createDate",
  																								"Создан",
  																								MetaPropertyType.DATETIME.getValue(),
  																								null,null,
  																								true,true,false,false,false,
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); //createDate
  		root.properties.add(new StoredPropertyMeta(4,
  		                                           "overdue",
  																								"Просрочен",
  																								MetaPropertyType.DATETIME.getValue(),
  																								null,null,
  																								true,true,false,false,false,
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); //overdue
  		
  		
  		Map<String, String> vstatuses = new LinkedHashMap<String, String>();
  		
  		vstatuses.put("f:created1","Новый");
  		vstatuses.put("f:signed","Подписан");
  		vstatuses.put("f:send2","Отправлен");
  		vstatuses.put("f:received3","Получен");
  		vstatuses.put("f:processed5","Обработан");
  		vstatuses.put("f:closed6","Закрыт");
  		vstatuses.put("f:expired7","Просрочен");
  		vstatuses.put("f:working","В обработке ведомством-получателем");
  		vstatuses.put("f:accept","Принято ведомством-получателем");
  		vstatuses.put("f:reciverClosed","Закрыт ведомством-получателем");
  		vstatuses.put("f:senderDecline","Отклонен ведомством-отправителем");
  		vstatuses.put("f:senderClose","Закрыт ведомством-отправителем");
  		
  		vstatuses.put("r:created1","Новый");
  		vstatuses.put("r:signed", "Подписан");
  		vstatuses.put("r:send2", "Отправлен");
  		vstatuses.put("r:received3", "Получен");
  		vstatuses.put("r:registered4", "Зарегистрирован");
  		vstatuses.put("r:processed5", "Обработан");
  		vstatuses.put("r:closed6", "Закрыт");
  		vstatuses.put("r:expired7", "Просрочен");
  		vstatuses.put("r:working", "В обработке ведомством-получателем");
  		vstatuses.put("r:accept", "Принято ведомством - получателем");
  		vstatuses.put("r:reciverClosed", "Закрыт ведомством - получателем");
  		vstatuses.put("r:senderDecline", "Отклонен ведомством-отправителем");
  		vstatuses.put("r:senderClose", "Закрыт ведомством-отправителем");
  		vstatuses.put("r:signedRes", "Подписан запрос результата");
  		vstatuses.put("r:inQuery", "В очереди на отправку");
  		vstatuses.put("r:unsend", "Не удалось отправить запрос из очереди");
  		vstatuses.put("r:rejected", "Отклонен");
  		vstatuses.put("r:canceled","Отменен");

  	/*	
  		vstatuses.put("p:1","Новое");
  		vstatuses.put("p:2","В обработке");
  		vstatuses.put("p:3","Обработанные");
  		vstatuses.put("p:5","Положительное решение");
  		vstatuses.put("p:6","Отказать");
  		vstatuses.put("p:7","На рассмотрении");
  		vstatuses.put("p:8","В обработке по МВ");
  		vstatuses.put("p:9","Отклонено");
  		vstatuses.put("p:10","Требуются оригиналы");
  		vstatuses.put("p:11","Принятые из ЛК");
  		vstatuses.put("p:12","Требуется оплата");
  		vstatuses.put("p:13","Ожидается подтверждение оплаты");
  		vstatuses.put("p:14","Требуется подтверждение оплаты оператором");
  		vstatuses.put("p:15","В обработке");
  		vstatuses.put("p:16","Результат отправки");
  		vstatuses.put("p:17","Не доставлено");
  		vstatuses.put("p:18","Техническая ошибка");
  		vstatuses.put("p:21","Проект");
  		vstatuses.put("p:22","Отклонено согласующим");
  	*/
  		
  		vstatuses.put("p:4","Новое");
  		vstatuses.put("p:1","В обработке");
  		vstatuses.put("p:5","Обработанные");
  		vstatuses.put("p:7","Положительное решение");
  		vstatuses.put("p:6","Отказать");
  		vstatuses.put("p:3","На рассмотрении");
  		vstatuses.put("p:2","В обработке по МВ");
  		vstatuses.put("p:9","Отклонено");
  		vstatuses.put("p:10","Требуются оригиналы");
  		vstatuses.put("p:11","Принятые из ЛК");
  		vstatuses.put("p:12","Требуется оплата");
  		vstatuses.put("p:14","Ожидается подтверждение оплаты");
  		vstatuses.put("p:15","Требуется подтверждение оплаты оператором");
  		vstatuses.put("p:16","В обработке");
  		vstatuses.put("p:2311","Результат отправки");
  		vstatuses.put("p:19","Не доставлено");
  		vstatuses.put("p:666","Техническая ошибка");
  		vstatuses.put("p:21","Проект");
  		vstatuses.put("p:22","Отклонено согласующим");	  		
  		
  		root.properties.add(new StoredPropertyMeta(5,
  		                                           "offlnVirtStatus",
  																								"Оффлайн. Статус",
  																								MetaPropertyType.STRING.getValue(),
  																								null,null,
  																								true,true,false,false,false,"",
  																								null,null,null,null,null,null,
  																								null,null,new StoredSelectionProvider(vstatuses),false,false,null,null)); // virt status
  		
  		root.properties.add(new StoredPropertyMeta(6,
  		                                           "offlnDestOrg",
  																								"Оффлайн. Принимающая организация",
  																								MetaPropertyType.GUID.getValue(),
  																								null,null,
  																								true,true,false,false,false,"",
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); // virt pettOrg
  
  		root.properties.add(new StoredPropertyMeta(7,
  		                                           "offlnSrcOrg",
  																								"Оффлайн. Отправляющая организация",
  																								MetaPropertyType.GUID.getValue(),
  																								null,null,
  																								true,true,false,false,false,"",
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); // virt rqstOrg		
		}
		
		if (pet != null) {
  		pet.properties.add(new StoredPropertyMeta(200,
 		                                           "offlnResolutionDocType",
 																								"Документ",
 																								MetaPropertyType.STRING.getValue(),
 																								null,null,
 																								true,false,false,false,false,"",
 																								null,null,null,null,null,null,
 																								null,null,null,false,false,null,null)); 	
  		pet.properties.add(new StoredPropertyMeta(210,
  		                                           "offlnResolutionDocFile",
  																								"Документ",
  																								MetaPropertyType.FILE.getValue(),
  																								null,null,
  																								true,false,false,false,false,"",
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); 	
  		pet.properties.add(new StoredPropertyMeta(220,
  		                                           "offlnResolutionComment",
  																								"Комментарий",
  																								MetaPropertyType.TEXT.getValue(),
  																								null,null,
  																								true,false,false,false,false,"",
  																								null,null,null,null,null,null,
  																								null,null,null,false,false,null,null)); 		
			
		}
		
		Properties hashes = new Properties();
		
		File dir = new File(hashDir);
		dir.mkdirs();
		File classHashFile = new File(dir,"ch_" + client + ".info");
		File viewDirFile = new File(this.viewsDir);
		if (!viewDirFile.isDirectory())
			viewDirFile = null;
		Gson gs = new GsonBuilder().serializeNulls().create();
		
		try {
			if (classHashFile.exists())
				hashes.load(new FileReader(classHashFile));
		} catch (IOException e) {
			e.printStackTrace(logger.Out());
			logger.FlushStream(ILogger.ERROR);
		}
		
		Map<String, String> definedNav = new TreeMap<String, String>();
		@SuppressWarnings("serial")
		final Map<String,Integer> taborder = new HashMap<String, Integer>(){{
			put("Данные запроса",0);
			put("Данные ответа",1);
			put("Общая информация",2);
			put("Системные",3);
		}};
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmmssSSS");
		Date d = new Date();
		
		for (StoredClassMeta cm : classes.values()) {
			boolean changed = itemChanged(gs, cm.name, cm, hashes);
			ClassAssembler ca = new ClassAssembler(cm, classes);
			if (changed) {
				logger.Info("class " + cm.name + " added to package");
				cm.version = sdf.format(d);
				classesDelta.add(cm);
				if (classNav.containsKey(cm.name)) {
					if (cm.ancestor != null) {
						String[] nav = classNav.get(cm.name);
						
						String[] parts = nav[0].split("\\|");
						
						if (parts[0].equals("federal") && ca.properties.containsKey("fsmevSender")){
							parts[0] = "rdata";
							nav[0] = String.join("|", parts);
						}
						
						if (!definedNav.containsKey(nav[0])) {
							String p = "";
							for (int i = 0; i < parts.length; i++) {
								String c = parts[i];
								if (i == 1) {
									if (nav[1] != null && !nav[1].isEmpty())
										c = nav[1];
								} else {
									if (classTypeTitles != null)
										if (classTypeTitles.containsKey(c))
											c = classTypeTitles.getProperty(c);
								}
								p = p + (p.isEmpty() ? "" : ".") + parts[i];
								changedNav.add(new StoredNavNode(0, p, c, null));
							}
							definedNav.put(nav[0], p);
						}
						changedNav.add(new StoredNavNode(0, nav[0].replace("|", ".") + "."
								+ cm.name, cm.caption, cm.name, null));
					} // else if (cm.key.equals("guid"))
						// System.out.println("Class "+cm.name+" navigable by " +
						// classNav.get(cm.name)[0] + " has a key " + cm.key +
						// " and was not binded to ancestor!");
				}
			}

			String listViewName = cm.name + "/list";
			StoredListViewModel listViewModel = loadModel(gs,
																										viewDirFile,
																										StoredListViewModel.class,
																										listViewName);
			Set<String> columnProps = new HashSet<String>();
			boolean needsave = false;
			if (listViewModel == null) {
				listViewModel = new StoredListViewModel(ViewApplyMode.HIDE.getValue());
				needsave = true;
			} else {
				for (StoredColumn col : listViewModel.columns) {
					columnProps.add(col.property);
				}
			}

			if (changed || needsave) {
				if (cm.ancestor != null && !cm.ancestor.isEmpty()
						|| cm.name.equals("SmevEntity")) {					
				
					if (!columnProps.contains("ouid"))
						listViewModel.columns.add(new StoredColumn("Номер",
																											 "ouid",
																											 FieldType.TEXT,
																											 FieldSize.TINY,
																											 true,
																											 false,
																											 0,""));

					if (!columnProps.contains("class"))
						listViewModel.columns.add(new StoredColumn("Тип", 
						                                           "class", 
						                                           FieldType.TEXT, 
						                                           null, 
						                                           true, 
						                                           false, 
						                                           1,""));

					if (!columnProps.contains("offlnVirtStatus"))
						listViewModel.columns.add(new StoredColumn("Статус",
																											 "offlnVirtStatus",
																											 FieldType.TEXT,
																											 FieldSize.SHORT,
																											 true,
																											 true,
																											 2,""));

					if (!columnProps.contains("timeStamp"))
						listViewModel.columns.add(new StoredColumn("Изменен",
																											 "timeStamp",
																											 FieldType.DATETIME_PICKER,
																											 FieldSize.TINY,
																											 true,
																											 true,
																											 3,""));

					if (!columnProps.contains("createDate"))
						listViewModel.columns.add(new StoredColumn("Создан",
																											 "createDate",
																											 FieldType.DATETIME_PICKER,
																											 FieldSize.TINY,
																											 true,
																											 true,
																											 4,""));
				} else {
					if (classAttrTabs.containsKey(cm.name)){
						Map<String, String> attrGroups = classAttrTabs.get(cm.name);
						for (StoredPropertyMeta pm : ca.properties.values()){
							if (attrGroups.containsKey(pm.name) && !columnProps.contains(pm.name)){
								listViewModel.columns.add(new StoredColumn(pm.caption,
																											 pm.name,
																											 FieldType.fromPropertyType(MetaPropertyType.fromInt(pm.type)),
																											 true,
																											 pm.order_number,""));
							}
						}
					}
				}

				if (changed)
					changedViews.put(listViewName, listViewModel);

				if (needsave)
					saveModel(gs, viewDirFile, listViewName, listViewModel);
			}

			needsave = false;
			if (classAttrTabs.containsKey(cm.name) && !cm.name.equals("rqstMain")) {
				String formViewName = cm.name + "/item";
				StoredFormViewModel formViewModel = loadModel(gs,
																											viewDirFile,
																											StoredFormViewModel.class,
																											formViewName);

				if (formViewModel == null) {
					formViewModel = new StoredFormViewModel(new LinkedList<StoredTab>(),
																									ViewApplyMode.HIDE.getValue(), HistoryDisplayMode.BYCLASS.getValue());
					needsave = true;
				}

				if (changed || needsave) {
					Map<String, List<StoredPropertyMeta>> groups = new TreeMap<String, List<StoredPropertyMeta>>();
										
					Map<String, String> attrTabs = classAttrTabs.get(cm.name);

					Set<String> excludeAttrs = new HashSet<String>();
					Map<String, Collection<StoredPropertyMeta>> groupedFileAttrs = new HashMap<String, Collection<StoredPropertyMeta>>();
					
					for (StoredPropertyMeta pm : ca.properties.values()) {
						if (pm.type == MetaPropertyType.FILE.getValue()){
							if (ca.properties.containsKey(pm.name + "Mime"))
								excludeAttrs.add(pm.name + "Mime");
							if (ca.properties.containsKey(pm.name + "Name"))
								excludeAttrs.add(pm.name + "Name");
							if (ca.properties.containsKey(pm.name + "Date"))
								excludeAttrs.add(pm.name + "Date");
							if (ca.properties.containsKey(pm.name + "Size"))
								excludeAttrs.add(pm.name + "Size");
							
							if (ca.properties.containsKey(pm.name + "Check")){
								excludeAttrs.add(pm.name + "Check");
								Collection<StoredPropertyMeta> flds = new LinkedList<StoredPropertyMeta>();
								flds.add(ca.properties.get(pm.name + "Check"));								
								flds.add(pm);
								groupedFileAttrs.put(pm.name, flds);
							}
						}
						
						if (attrTabs.containsKey(pm.name)) {
							String attrTab = attrTabs.get(pm.name);
							if (!groups.containsKey(attrTab))
								groups.put(attrTab, new LinkedList<StoredPropertyMeta>());
							List<StoredPropertyMeta> l = groups.get(attrTab);
							l.add(pm);
						}
					}

					Map<String, Map<String, StoredField>> existingFullGroupFields = new TreeMap<String, Map<String, StoredField>>();

					Map<String, Map<String, StoredField>> existingShortGroupFields = new TreeMap<String, Map<String, StoredField>>();					
					
					Map<String, Collection<StoredField>> existsFullFields = getViewModelFields(formViewModel,
																																										 true, existingFullGroupFields);
					Map<String, Collection<StoredField>> existsShortFields = getViewModelFields(formViewModel,
																																											false, existingShortGroupFields);

					for (Map.Entry<String, List<StoredPropertyMeta>> group : groups.entrySet()) {
						StoredTab tab = getTabByName(formViewModel.tabs, group.getKey());
						
						boolean fro = false;
						
						if (group.getKey().equals("Общая информация") && cm.ancestor != null)
							fro = true;
						else if (group.getKey().equals("Данные запроса") && cm.ancestor != null && cm.ancestor.equals("Petition"))
							fro = true;
						else if (group.getKey().equals("Данные ответа") && cm.ancestor != null && cm.ancestor.equals("FederalService"))
							fro = true;
						
						if (tab == null) {
							tab = new StoredTab(group.getKey(), new LinkedList<StoredField>());
							formViewModel.tabs.add(tab);
						}
						
						Map<String, StoredField> tabExistingFullGroupFields = existingFullGroupFields.containsKey(tab.caption)?existingFullGroupFields.get(tab.caption):new TreeMap<String, StoredField>();
						Map<String, StoredField> tabExistingShortGroupFields = existingShortGroupFields.containsKey(tab.caption)?existingShortGroupFields.get(tab.caption):new TreeMap<String, StoredField>();
						
						for (StoredPropertyMeta pm : group.getValue()) 
							if (!(pm.name.equals("resolution") && (cm.ancestor != null) && cm.ancestor.equals("Petition"))){
								boolean isDictionary = (_dictionaries.containsKey(cm.name) && 
										_dictionaries.get(cm.name).contains(pm.name)) ? true : false;
  							applyField(existsFullFields, cm, tab, pm, classes, groupedFileAttrs, true, fro, 
								           (cm.ancestor != null) && cm.ancestor.equals("Petition"), classAttrGroups.get(cm.name), tabExistingFullGroupFields, isDictionary);
  							if (pm.type == MetaPropertyType.STRING.getValue()) {
  								applyField(existsShortFields, cm, tab, pm, classes, groupedFileAttrs, false, fro, 
									           (cm.ancestor != null) && cm.ancestor.equals("Petition"), classAttrGroups.get(cm.name), tabExistingShortGroupFields, false);
  							}
							}
						
						if (group.getKey().equals("Данные ответа") && (cm.ancestor != null) && cm.ancestor.equals("Petition")){
							if (!existsFullFields.containsKey("offlnResolutionDecision")){
								StoredField field = new StoredField("Резолюция", null, FieldType.GROUP, 0,"", null);
								field.fields.add(new StoredField("Решение", "offlnResolutionDecision", 0,""));
								field.fields.add(new StoredField("Документ", "offlnResolutionDocFile", FieldType.FILE, 1, "", null));
								field.fields.add(new StoredField("Комментарий", "offlnResolutionComment", FieldType.MULTILINE, 2, "", null));
								tab.fullFields.add(field);	
							}
							
							if (!existsShortFields.containsKey("offlnResolutionDecision")){
								tab.shortFields.add(new StoredField("Резолюция", "offlnResolutionDecision", 0,""));
							}
						}
					}
					
					
					((List)formViewModel.tabs).sort(new Comparator<StoredTab>() {
						@Override
						public int compare(StoredTab o1, StoredTab o2) {
							Integer i1 = 10;
							Integer i2 = 10;
							if (taborder.containsKey(o1.caption))
								i1 = taborder.get(o1.caption);
							if (taborder.containsKey(o2.caption))
								i2 = taborder.get(o2.caption);
							return i1 - i2;
						}
					});
					
					saveModel(gs, viewDirFile, formViewName, formViewModel);
					
					if (changed)
						changedViews.put(formViewName, formViewModel);

				}
			}
		}
		
		Writer w = null;
		try {
			w = new FileWriter(classHashFile);
			hashes.store(w, "");
			w.close();
		} catch (IOException e) {
			logger.Error("", e);
		} finally {
			if (w != null)
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace(logger.Out());
					logger.FlushStream(ILogger.ERROR);
				}
		}		
	}  

	private void applyField(Map<String, Collection<StoredField>> existsFields, StoredClassMeta cm,
	                        StoredTab tab, StoredPropertyMeta pm,
                          Map<String, StoredClassMeta> classes,
                          Map<String, Collection<StoredPropertyMeta>> groupedFileAttrs, 
                          boolean useFullFields, boolean forceReadOnly, boolean forPetition, 
                          Map<String, String> attrGroups, Map<String,StoredField> groupFields, boolean isDictionary) {
		FieldType newFieldType = FieldType.fromPropertyType(MetaPropertyType.fromInt(pm.type));
		
		if (pm.selection_provider != null && newFieldType != FieldType.REFERENCE)
			newFieldType = FieldType.TEXT;
		
  	String pname = pm.name;
  	if (pm.type == MetaPropertyType.STRUCT.getValue())
  		if (pm.ref_class != null && pm.ref_class.equals("cmsFile")){
  			pname = pm.name+"$body";
  			newFieldType = FieldType.FILE;
  		}

  	if (existsFields.containsKey(pname)) {
	  	Collection<StoredField> fields = existsFields.get(pname);
	  	for (StoredField field : fields) {
	  		boolean maskExist = (field.mask != null && !field.mask.isEmpty()) || (field.maskName != null && !field.maskName.isEmpty());
	  		if (!maskExist)
	  			field.type = newFieldType.getValue();
	  		field.readonly = pm.readonly || forceReadOnly;
	  		field.required = !pm.nullable;
	  		if (field.type == FieldType.COLLECTION.getValue() && (field.commands == null || field.commands.isEmpty())){
  	  		field.commands = new LinkedList<StoredAction>();
  	  		field.commands.add(new StoredAction(ActionType.CREATE.name(), "Создать", null, null, false, false, false));
  	  		field.commands.add(new StoredAction(ActionType.EDIT.name(), "Изменить", null, null, false, false, false));
  	  		field.commands.add(new StoredAction(ActionType.DELETE.name(), "Удалить", null, null, false, false, false));	  			
	  		}
	  	}
	  } else {
	  	StoredField field = null;
	  	if (groupedFileAttrs.containsKey(pm.name)){
	  		Collection<StoredField> fields = new LinkedList<StoredField>(); 
	  		String caption = pm.caption;
	  		for (StoredPropertyMeta gpm: groupedFileAttrs.get(pm.name)){
	  			String capt = gpm.caption;
	  			
	  			if (gpm.name.equals(pm.name))
	  				capt = "Файл";
	  			else if (gpm.name.equals(pm.name + "Check")){
	  				caption = gpm.caption;
	  				capt = "Прикреплен";
	  			}
	  				
	  			fields.add(new StoredField(capt, gpm.name, 
	  			                           FieldType.fromPropertyType(MetaPropertyType.fromInt(gpm.type)), 
	  			                           gpm.order_number, !gpm.nullable, gpm.readonly || forceReadOnly, gpm.hint));
	  		}
	  		field = new StoredField(caption, fields, "");
	  	} else if (pm.type == MetaPropertyType.STRUCT.getValue()) {
	  		if (classes.containsKey(pm.ref_class)){
	  			if (pm.ref_class.equals("cmsFile"))
	  				field = new StoredField(pm.caption, pm.name+"$body", FieldType.FILE, pm.order_number, !pm.nullable, pm.readonly || forceReadOnly, pm.hint);
	  			else
		  			field = new StoredField(pm.caption, pm.name, FieldType.GROUP, pm.order_number, !pm.nullable, pm.readonly || forceReadOnly, pm.hint);
	  		}
	  	} else {
	  		field = new StoredField(pm.caption, pm.name, newFieldType, pm.order_number, !pm.nullable, pm.readonly || forceReadOnly, pm.hint);
  	  	
	  		if (field.type == FieldType.COLLECTION.getValue()){
  	  		field.mode = CollectionFieldMode.TABLE.getValue();
  	  		field.actions = ActionType.CREATE.getValue() | ActionType.EDIT.getValue() | ActionType.DELETE.getValue();
  	  		field.commands = new LinkedList<StoredAction>();
  	  		
  	  		field.commands.add(new StoredAction(ActionType.CREATE.name(), "Создать", null, null, false, false, false));
  	  		field.commands.add(new StoredAction(ActionType.EDIT.name(), "Изменить", null, null, false, false, false));
  	  		field.commands.add(new StoredAction(ActionType.DELETE.name(), "Удалить", null, null, false, false, false));
  	  		
  	  		if (pm.back_ref != null && pm.back_ref.equals("offlnSvBref")){
  	  			field.columns.add(new StoredColumn("", "offlnSvVal",""));
  	  		} else if (classCollectionColumns.containsKey(cm.name)) {
  	  			Map<String, Collection<String>> attrCols = classCollectionColumns.get(cm.name);
  	  			if (attrCols.containsKey(pm.name) && classes.containsKey(pm.items_class)) {
  	  				Collection<String> colNames = attrCols.get(pm.name);
  	  				ClassAssembler colCm = new ClassAssembler(classes.get(pm.items_class), classes);
  	  				for (String colName: colNames) 
  	  				if (colCm.properties.containsKey(colName)) {
  	  					field.columns.add(new StoredColumn(colCm.properties.get(colName).caption, colName, ""));
  	  				}
  	  			}
  	  		}
  	  	}
  	  	
  	  	if(field.type == FieldType.REFERENCE.getValue() && isDictionary){
  	  		field.actions = 0;
  	  		field.commands = new LinkedList<StoredAction>();
  	  		/*
  	  		if (cm.name.equals("rqstMain") && (pm.equals("rqstTyp") || pm.equals("pettOrg")))
  	  			field.selection_paginated = false;
  	  		*/	
  	  	}
	  	}
	  	
	  	if (attrGroups != null && attrGroups.containsKey(pm.name) && !attrGroups.get(pm.name).isEmpty()){
	  		String gPath = attrGroups.get(pm.name);
	  		StoredField groupedField = field;
	  		StoredField parent = null;

	  		int begin = 0;
	  		int end = gPath.indexOf("|", begin);
	  		if (end < 0) end = gPath.length();
	  		
	  		while (begin < gPath.length()) {
	  			
  	  		String sPath = gPath.substring(0, end);
  	  		
	  			if (!groupFields.containsKey(sPath)){
	  				StoredField nf = new StoredField(gPath.substring(begin,  end), null, FieldType.GROUP, field.order_number,"",null);
	  				
	  				if (parent == null)
	  					field = nf;
	  				else
	  					parent.fields.add(nf);
	  				
	  				groupFields.put(sPath, nf);
	  				
  					parent = nf;
	  			} else
	  				parent = groupFields.get(sPath);
	  				
	  			begin = end + 1;
	  			end = gPath.indexOf("|", begin);
	  			if (end < 0) end = gPath.length();
	  		}
	  		
	  		if (groupFields.get(gPath) == null){
	  			field = groupedField;
	  			logger.Warning("При размещении поля для атрибута " + cm.name + "." + pm.name + " не удалось найти групповое поле для пути '"+gPath+"'!");
	  		} else {
	  			groupFields.get(gPath).fields.add(groupedField);
	  			if (field == groupedField)
	  				return;
	  		}
	  	}
	  	
	  	if (useFullFields)
	  		tab.fullFields.add(field);
	  	else
	  		tab.shortFields.add(field);
	  }
  }

	/** Возвращает "имя свойства" -> "список сопоставленных полей модели просмотра" */
	private Map<String, Collection<StoredField>> getViewModelFields(StoredFormViewModel model, boolean useFullFields, Map<String, Map<String, StoredField>> groupFields) {
		Map<String, Collection<StoredField>> result = new HashMap<String, Collection<StoredField>>();
		for (StoredTab tab : model.tabs) {
			Collection<StoredField> fields = useFullFields?tab.fullFields:tab.shortFields;
			Map<String, StoredField> gFields = new TreeMap<String, StoredField>();
			if(groupFields != null)				
				groupFields.put(tab.caption, gFields);
			for (StoredField field: fields) {
				getExistingField(field, result, null, gFields);
			}
		}
		return result;
	}
	
	private void getExistingField(StoredField field, Map<String, Collection<StoredField>> pFields, String prefix, Map<String, StoredField> gFields) {
		if(field.property != null && !field.property.isEmpty()){
			Collection<StoredField> sameFields = null;
			if (pFields.containsKey(field.property)) {
				sameFields = pFields.get(field.property);
			} else {
				sameFields = new LinkedList<StoredField>();
				pFields.put(field.property, sameFields);
			}
			sameFields.add(field);
		} else if(field.type == FieldType.GROUP.getValue()) {
			String group_path = ((prefix != null && !prefix.isEmpty())?prefix+".":"")+field.caption;
			gFields.put(group_path, field);
			for(StoredField fld : field.fields){
				getExistingField(fld, pFields, group_path, gFields);
			}
		}
	}
	
	private StoredTab getTabByName(Collection<StoredTab> tabs, String caption) {
		for(StoredTab tab:tabs)
			if (tab.caption.equalsIgnoreCase(caption))
				return tab;
		return null;
	}
	
	private <T> T loadModel(Gson gson, File viewDirFile, Class<T> cls, String viewName) {
		if (viewDirFile == null)
			return null;
		File viewFile = new File(viewDirFile, viewName + ".json");
		if (!viewFile.isFile())
			return null;
		try {
			T result = null;
			Reader r = new InputStreamReader(new FileInputStream(viewFile), "UTF-8");
			try {
				result = gson.fromJson(r, cls);
			} finally {
				r.close();
			}
			return result;
		} catch (Exception e) {
			logger.Error("Не удалось загрузить модель представления.",e);
			return null;
		}
	}

	private void saveModel(Gson gson, File viewDirFile, String viewName, StoredViewModel model) {
		if (viewDirFile == null)
			return;
			// Если папка не была указана, то не должны попасть на вызов этого метода
			//throw new AssertionError();
		File viewFile = new File(viewDirFile, viewName + ".json");
		viewFile.getParentFile().mkdirs();
		try {
			Writer w = new OutputStreamWriter(new FileOutputStream(viewFile, false), "UTF-8");
			try {
				gson.toJson(model, w);
			} finally {
				w.close();
			}
		} catch (Exception e) {
			logger.Error("Не удалось сохранить модель представления.",e);
		}
	}
	
	@SuppressWarnings("serial")
	public SyncDelta getDelta(String client, String[] users, Date since, Integer syncHorizon)
			throws IonException {
		List<UserProfile> profiles = new LinkedList<UserProfile>();
		Set<String> classNames = new HashSet<String>();
		Map<String, String[]> classNav = new TreeMap<String, String[]>();
		final Map<String, Set<String>> classOrgs = new TreeMap<String, Set<String>>();
		Map<String, String> classTypes = new TreeMap<String, String>();
		
		formProfiles(client, users, since, profiles, classNames, classOrgs, classTypes, classNav);
				
		List<StoredClassMeta> classesDelta = new LinkedList<StoredClassMeta>();
		List<StoredNavNode> changedNav = new LinkedList<StoredNavNode>();
		Map<String, StoredViewModel> changedViews = new TreeMap<String, StoredViewModel>();
		Map<String, Integer> dictPermissions = new TreeMap<String, Integer>();
		
		final Map<String, StoredClassMeta> classes = new TreeMap<String, StoredClassMeta>();
		//final Map<String, Set<String>> eagerLoads = new HashMap<String, Set<String>>();
		
		final Map<String, String> classWsdl = new TreeMap<String, String>();
		
		formClassDelta(client,/* since,*/ classNames, profiles, classOrgs, classTypes, classNav, classes, classWsdl, classesDelta, changedNav, changedViews, dictPermissions);
		
		Properties hashes = new Properties();
		
		File dir = new File(hashDir);
		dir.mkdirs();
		File profileHashFile = new File(dir,"ph_" + client + ".info");
		Gson gs = new GsonBuilder().serializeNulls().create();
		
		try {
			if (profileHashFile.exists())
				hashes.load(new FileReader(profileHashFile));
		} catch (IOException e) {
			e.printStackTrace(logger.Out());
			logger.FlushStream(ILogger.ERROR);
		}
		
		UserProfile[] tmpProf = profiles.toArray(new UserProfile[profiles.size()]);
		
		for (UserProfile p: tmpProf){
			for (Map.Entry<String, Integer> ap: dictPermissions.entrySet())
				if (!p.access.containsKey(ap.getKey()))
						p.access.put(ap.getKey(), ap.getValue());
			
			if (!itemChanged(gs, p.login, p, hashes))
				profiles.remove(p);
		}
		
		Writer w = null;
		try {
			w = new FileWriter(profileHashFile);
			hashes.store(w, "");
			w.close();
		} catch (IOException e) {
			logger.Error("", e);
		} finally {
			if (w != null)
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace(logger.Out());
					logger.FlushStream(ILogger.ERROR);
				}
		}
				
		final Iterator<StoredClassMeta> c = classes.values().iterator();
		
		final boolean firstPackage = (since == null);
		
		Date snc = (syncHorizon == null) ? since : new Date(new Date().getTime() - syncHorizon*3600000);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		final String sinceStr = (snc != null)?format.format(snc):null;
		
		Iterator<DataUnit> du = new Iterator<DataUnit>() {
			
			Collection<DataUnit> curCollection = null;
			Iterator<DataUnit> curIter = null;

			@Override
			public boolean hasNext() {
				while ((curIter == null || !curIter.hasNext()) && c.hasNext()){
					StoredClassMeta cm = c.next();
					while (cm != null && !(classWsdl.containsKey(cm.name)
							&& (
									(initDictionaries.contains(cm.name) && firstPackage) || 
									(loadDictionaries 
										&& !dictionaryExcludeList.contains(cm.name)  
										&& (dictionaryIncludeList.isEmpty() || dictionaryIncludeList.contains(cm.name))
									|| cm.ancestor != null && !cm.ancestor.isEmpty())
									)
							)){
						if (!classWsdl.containsKey(cm.name))
							logger.Warning("Адрес wsd для класса " + cm.name + " не был найден. Возможно класс не опубликован.");
						logger.Info("Объекты класса " + cm.name + " не будут загружены.");
						cm = c.hasNext()?c.next():null;
					}
					if (cm != null) {
						curCollection = new LinkedList<DataUnit>();
						try {
  						ClassAssembler ca = new ClassAssembler(cm, classes);
  						Map<String, Object> filter = new HashMap<String, Object>();
  						
  						Integer pageCount = null;
  						
  						if (initDictionaries.contains(cm.name) && firstPackage) {
  							pageCount = 0;
  						}
  						
							if (ca.properties.containsKey("timeStamp") && (sinceStr != null) && 
									!(initDictionaries.contains(cm.name) && firstPackage))
								filter.put("timeStamp", sinceStr);
  						
  						if (classOrgs.containsKey(cm.name)){
  								if (requestor instanceof SitexSoapRequestor2){
  									if (ca.properties.containsKey("rqstOrg")){
  										filter.put("rqstOrg", classOrgs.get(cm.name));
  										curCollection.addAll(requestor.fetchData(ca, filter, classes, pageCount));
  										filter.remove("rqstOrg");
  									}
  									if (ca.properties.containsKey("pettOrg")) {
  										filter.put("pettOrg", classOrgs.get(cm.name));
  										curCollection.addAll(requestor.fetchData(ca, filter, classes, pageCount));
  										filter.remove("pettOrg");
  									} else if (ca.properties.containsKey("recipientOrg")) {
  										filter.put("recipientOrg", classOrgs.get(cm.name));
  										curCollection.addAll(requestor.fetchData(ca, filter, classes, pageCount));
  										filter.remove("recipientOrg");
  									}
  								} else {
    								for (String org: classOrgs.get(cm.name)){
    									if (ca.properties.containsKey("rqstOrg")){
    										filter.put("rqstOrg", org);
    										curCollection.addAll(requestor.fetchData(ca, filter, classes, pageCount));
    										filter.remove("rqstOrg");
    									}
    									if (ca.properties.containsKey("pettOrg")) {
    										filter.put("pettOrg", org);
    										curCollection.addAll(requestor.fetchData(ca, filter, classes, pageCount));
    										filter.remove("pettOrg");
    									} else if (ca.properties.containsKey("recipientOrg")) {
    										filter.put("recipientOrg", org);
    										curCollection.addAll(requestor.fetchData(ca, filter, classes, pageCount));
    										filter.remove("recipientOrg");
    									}
    								}
  								}
  						} else {
  							curCollection.addAll(requestor.fetchData(ca, filter, classes, pageCount));
  						}
						} catch (Exception e) {
							e.printStackTrace(logger.Out());
							logger.FlushStream(ILogger.ERROR);
						}
					}
					
					if (curCollection != null && curCollection.size() > 0)
						curIter = curCollection.iterator();
					else {
						curIter = null;
					}
				}
				
				if (curIter != null)
					return curIter.hasNext();
				return false;
			}

			@Override
			public DataUnit next() {
				return curIter.next();
			}
		};

		Set<String> signedClasses = new HashSet<String>();
		
		//if (classesDelta.isEmpty())
		for (StoredClassMeta cm: classes.values()){
			try {
				if (cm.ancestor != null && !cm.ancestor.isEmpty() &&
						!digTplExcludeList.contains(cm.name) &&
						(digTplIncludeList.isEmpty() || digTplIncludeList.contains(cm.name)))
					signedClasses.add(cm.name);
			} catch (Exception e) {
					e.printStackTrace(logger.Out());
					logger.FlushStream(ILogger.ERROR);
			}
		}
		
		final Iterator<DataUnit> digestTpl = getSignatureTemplateDelta(client, signedClasses);
				
		if (!profiles.isEmpty() || !classesDelta.isEmpty() || !changedNav.isEmpty() || du.hasNext() || digestTpl.hasNext())
			return new SyncDelta(profiles.toArray(new UserProfile[profiles.size()]), 
				(classesDelta.size() > 0)?new MetaDelta(classesDelta.toArray(new StoredClassMeta[classesDelta.size()])):null, 
				(changedNav.size() > 0)?new NavigationDelta(changedNav.toArray(new StoredNavNode[changedNav.size()])):null,
				(changedViews.size() > 0)?new ViewDelta(changedViews):null,		
				(du.hasNext())?new DataDelta(du):null,
				digestTpl.hasNext()?new TreeMap<String, Iterator<DataUnit>>(){{
					put("DigestTemplates",digestTpl);
				}}:null);
				
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private String serializeDigestTemplate(DataUnit dt){
		String result = dt.data.get("template").toString();
		if (dt.data.containsKey("conditions")){
			result = result + "|";
			for (String[] c: ((Collection<String[]>)dt.data.get("conditions"))){
				result = result + "[" + c[0] + "," + c[1] + "]";
			}
		}
		if (dt.data.containsKey("params")){
			result = result + "|";
			for (String a: (List<String>)dt.data.get("params")){
				result = result + a + ",";
			}
		}		
		if (dt.data.containsKey("attrs")){
			result = result + "|";
			for (Map.Entry<String, String> a: ((Map<String, String>)dt.data.get("attrs")).entrySet()){
				result = result + "[" + a.getKey() + "=" + a.getValue() + "]";
			}
		}		
		return result;
	}
	
		
	private Iterator<DataUnit> getSignatureTemplateDelta(String client, Set<String> classNames) {
		
		File dir = new File(digestTemplateHashDir);
		dir.mkdirs();
		final File hashFile = new File(dir,"dth_" + client + ".info");
		
		final Properties hashes = new Properties();
		
		try {
			if (hashFile.exists())
				hashes.load(new FileReader(hashFile));
		} catch (IOException e) {
			e.printStackTrace(logger.Out());
			logger.FlushStream(ILogger.ERROR);
		}
		
		final Iterator<String> cnIter = classNames.iterator();
		
		return new Iterator<DataUnit>() {
			
			Iterator<DataUnit> curIter = null; 
			
			DataUnit u = null;
			
			private void tryFetchCurIter(){
				if (u == null){
  				if (curIter != null){
  					 if (curIter.hasNext()){
  							u = curIter.next();
  							try {
  			  				String hash = hasher.hash(serializeDigestTemplate(u));
  			  				while (hash.equals(hashes.getProperty(u.className+"."+u.id))){
  			  					hashes.put(u.className+"."+u.id, hash);
  			  					if (curIter.hasNext()){
  			  						u = curIter.next();
  			  						hash = hasher.hash(serializeDigestTemplate(u));
  			  					} else {
  			  						u = null;
  			  						break;
  			  					}
  			  				}
  			  				if (u != null)
  			  					hashes.put(u.className+"."+u.id, hash);
  							} catch(IonException e) {
  								e.printStackTrace(logger.Out());
  								logger.FlushStream(ILogger.ERROR);
  							}
  					 }
  				}
				}
			}
			

			@Override
			public boolean hasNext() {
				
				tryFetchCurIter();
				
				if (u != null)
					return true;
				
				String cn = "";
				while ((curIter == null || !curIter.hasNext()) && cnIter.hasNext()){
					try {
						cn = cnIter.next();
						logger.Info("Loading signature templates for "+cn);
						curIter = requestor.requestSignatureTemplate(cn, digestTplEncoding).iterator();
					} catch (IonException e1) {
						curIter = null;
						e1.printStackTrace(logger.Out());
						logger.FlushStream(ILogger.ERROR);
					}
					
					tryFetchCurIter();
					
					if (u != null)
						return true;
				}
				
				Writer w = null;
				try {
					w = new FileWriter(hashFile);
					hashes.store(w, "");
					w.close();
				} catch (IOException e) {
					logger.Error("", e);
				} finally {
					if (w != null)
						try {
							w.close();
						} catch (IOException e) {
							e.printStackTrace(logger.Out());
							logger.FlushStream(ILogger.ERROR);
						}
				}
					
				return false;
			}

			@Override
			public DataUnit next() {
				DataUnit res = u;
				u = null;
				return res;
			}
		};
	}
	
	private boolean itemChanged(Gson gs, String id, Object item, Properties hashes) throws IonException {
		String base = gs.toJson(item);
		
		/*
		String base = cm.is_struct+"|"+cm.name+"|"+cm.caption+"|"+cm.key+"|"+cm.ancestor+"|";
		for (StoredPropertyMeta pm: cm.properties){
			base = base + ">" + pm.name + "|" + pm.caption + "|" + pm.order_number +
					"|" + pm.type + "|" + pm.autoassigned + "|" + pm.items_class +
					"|" + pm.ref_class + "|" + pm.back_ref;
			if (pm.selection_provider != null){
					base = base + "|[" + pm.selection_provider.type + "|" + pm.selection_provider.hq + "|";
					for (StoredMatrixEntry me: pm.selection_provider.matrix){
						base = base + "(";
						for (StoredCondition sc: me.conditions)
							base = base + sc.property+"="+sc.value+"|";
						base = base + "),(";
						for (StoredKeyValue kv: me.result){
							base = base + kv.key+"="+kv.value+"|";
						}
						base = base + ")";
					}

					base = base + "|(";
					
					for (StoredKeyValue kv: pm.selection_provider.list){
						base = base + kv.key + "=" +kv.value+"|";
					}
										
					base = base + ")|(";
					
					for (StoredKeyValue kv: pm.selection_provider.parameters){
						base = base + kv.key + "=" +kv.value+"|";
					}
					
					base = base + ")]";
			}
		}
		*/
		
		
		
		String hash = hasher.hash(base);
		boolean result = true;
		if (hashes.containsKey(id)){
			//result = false;
			if (hashes.getProperty(id).equals(hash))
				result = false;
		}
		hashes.setProperty(id, hash);
		return result;
	}

	public ActionResult push(String authToken, DataUnit data)
			throws IonException {
		try {
			if ((data instanceof DataChange) && !data.className.equals("DigitalSignature")){
				if (data.data.containsKey("offlnResolutionDecision") && (data.data.get("offlnResolutionDecision") != null)){
					DataChange base = (DataChange)data;

					Map<String, Object> tmpdata = new HashMap<String, Object>();
					
					if (base.data.containsKey("offlnResolutionDecision"))
						tmpdata.put("description", base.data.get("offlnResolutionDecision"));
										
					DataChange resoldu = new DataChange(base.author, DataChangeType.CREATE.getValue(), null, "smevResolution", tmpdata);
					Map<String, String> resol = requestor.push(resoldu, authToken, _dictionaries, _collectionLinkClasses);
					
					String[] docTypeParams = requestor.fetchResolutionDescDoctype(base.data.get("offlnResolutionDecision").toString(), _dictionaries, _collectionLinkClasses);
					if(docTypeParams[0] == null || docTypeParams[1] == null)
						throw new IonException("Не удалось определить класс документа резолюции!");
					
					tmpdata.clear();
					tmpdata.put(docTypeParams[1], base.data.get("offlnResolutionDocFile"));
					if (docTypeParams[2] != null)
						tmpdata.put(docTypeParams[2], base.data.get("offlnResolutionComment"));
					
					DataChange docdu = new DataChange(base.author, DataChangeType.CREATE.getValue(), null, docTypeParams[0], tmpdata);					
					Map<String, String> doc = requestor.push(docdu, authToken, _dictionaries, _collectionLinkClasses);
					
					tmpdata.clear();
					
					Map<String, StoredClassMeta> classes = new HashMap<String, StoredClassMeta>();
					Map<String, String> classWsdl = new HashMap<String, String>();
					
					StoredClassMeta linkClass = requestor.requestClass("smevDocsInRes", classes, classWsdl);
					ClassAssembler linkClassA = new ClassAssembler(linkClass, classes);
					
					if (linkClassA.properties.containsKey("fromId") && linkClassA.properties.containsKey("toId")){
						String v = resol.get("ouid");
						if (resol.containsKey("guid") && 
								(linkClassA.properties.get("fromId").type == MetaPropertyType.REFERENCE.getValue() ||
								 linkClassA.properties.get("fromId").type == MetaPropertyType.STRING.getValue())){
							v = resol.get("guid");
						}

						tmpdata.put("fromId", v);
												
						v = doc.get("ouid");
						if (doc.containsKey("guid") && 
								(linkClassA.properties.get("toId").type == MetaPropertyType.REFERENCE.getValue() ||
								 linkClassA.properties.get("toId").type == MetaPropertyType.STRING.getValue())){
							v = doc.get("guid");
						}						
						
						tmpdata.put("toId", v);
					
						DataChange assocdu = new DataChange(base.author, DataChangeType.CREATE.getValue(), null, "smevDocsInRes", tmpdata);					
						requestor.push(assocdu, authToken, _dictionaries, _collectionLinkClasses);
						
						if (!data.data.containsKey("state") && base.data.containsKey("offlnVirtStatus") && (base.data.get("offlnVirtStatus") != null)){
							data.data.put("state", base.data.get("offlnVirtStatus").toString().replaceFirst("\\Ap:", ""));
						}
					}
					data.data.put("resolution", resol.get("guid"));
				}
			}
			
			requestor.push(data, authToken, _dictionaries, _collectionLinkClasses);
			return new ActionResult();
		} catch (Exception e) {
			logger.Error("Ошибка отправки объекта.", e);
			if (debug){
				e.printStackTrace(logger.Out());
				logger.FlushStream(ILogger.ERROR);
			}
			return new ActionResult(e.getMessage());
		}
	}
	
	public void setPpuUrl(String url){
		requestor.setPpuUrl(url);
	}
	
	public void setUserWsdl(String userWsdl) {
		requestor.setUserWsdl(userWsdl);
	}
	
	public void setMainWsdl(String mainWsdl) {
		((SitexSoapRequestor2)requestor).setMainWsdl(mainWsdl);
	}	
	
	public void setEagerClasses(Set<String> ec) {
		requestor.setEagerClasses(ec);
	}
	
	public String getHashDir() {
		return hashDir;
	}

	public void setHashDir(String hashFile) {
		this.hashDir = hashFile;
	}

	public String getDigestTemplateHashDir() {
		return digestTemplateHashDir;
	}

	public void setDigestTemplateHashDir(String digestTemplateHashDir) {
		this.digestTemplateHashDir = digestTemplateHashDir;
	}

	public IHashProvider getHasher() {
		return hasher;
	}

	public void setHasher(IHashProvider hasher) {
		this.hasher = hasher;
	}

	public void setMaxPageCount(int max){
		requestor.setMaxPageCount(max);
	}
	
	public void setConnectTimeout(int sec){
		requestor.setSoapConnectionTimeout(sec);
	}
	
	public void setReadTimeout(int sec){
		requestor.setSoapReadTimeOut(sec);
	}	

	public Properties getClassTypeTitles() {
		return classTypeTitles;
	}

	public void setClassTypeTitles(Properties classTypeTitles) {
		this.classTypeTitles = classTypeTitles;
	}

	public boolean isLoadDictionaries() {
		return loadDictionaries;
	}

	public void setLoadDictionaries(boolean loadDictionaries) {
		this.loadDictionaries = loadDictionaries;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
		requestor.setDebug(this.debug);
	}

	public void setSysLogin(String login) {
		requestor.setSysLogin(login);
	}

	public void setSysToken(String token) {
		requestor.setSysToken(token);
	}
	
	public void setViewsDir(String value) {
		this.viewsDir = value;
  }
	
	public Set<String> getDictionaryExcludeList() {
		return dictionaryExcludeList;
	}

	public void setDictionaryExcludeList(Set<String> dictionaryExcludeList) {
		this.dictionaryExcludeList = dictionaryExcludeList;
		this.requestor.setDictionaryExcludeList(dictionaryExcludeList);
	}
	
	public Set<String> getDictionaryIncludeList() {
		return dictionaryIncludeList;
	}

	public void setDictionaryIncludeList(Set<String> dictionaryIncludeList) {
		this.dictionaryIncludeList = dictionaryIncludeList;
		this.requestor.setDictionaryIncludeList(dictionaryIncludeList);
	}
	
	public String getDigestTplEncoding() {
		return digestTplEncoding;
	}

	public void setDigestTplEncoding(String digestTplEncoding) {
		this.digestTplEncoding = digestTplEncoding;
	}
	
	public void setDigestTemplatesUrl(String url){
		this.requestor.setSignaturesFetchWsdl(url);
		this.requestor.setSignaturesAttachWsdl(url);
	}
	
	public void setSignatureAttachType(Map<String, String> attachTypes){
		this.requestor.setSignatureAttachType(attachTypes);
	}

	public void setDigTplExcludeList(Set<String> digTplExcludeList) {
		this.digTplExcludeList = digTplExcludeList;
	}

	public void setDigTplIncludeList(Set<String> digTplIncludeList) {
		this.digTplIncludeList = digTplIncludeList;
	}	

	public void setInitDictionaries(List<String> initDictionaries) {
		this.initDictionaries = initDictionaries;
	}
	
}
