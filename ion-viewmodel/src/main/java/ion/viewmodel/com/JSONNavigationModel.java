package ion.viewmodel.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.FilterOption;
import ion.core.IMetaRepository;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.Sorting;
import ion.core.SortingMode;
import ion.core.logging.ILogger;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSorting;
import ion.framework.validators.JSONValidator;
import ion.viewmodel.navigation.ClassNode;
import ion.viewmodel.navigation.ContainerNode;
import ion.viewmodel.navigation.GroupNode;
import ion.viewmodel.navigation.HyperlinkNode;
import ion.viewmodel.navigation.IGroupNode;
import ion.viewmodel.navigation.INavigationModel;
import ion.viewmodel.navigation.INavigationSection;
import ion.viewmodel.navigation.INode;
import ion.viewmodel.navigation.NavigationSection;
import ion.viewmodel.navigation.NavigationSectionMode;
import ion.viewmodel.navigation.NodeType;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredNavSection;
import ion.viewmodel.plain.StoredPathChain;

public class JSONNavigationModel implements INavigationModel {
	
	private Map<String, INode> nodes;
	
	Map<String, INavigationSection> sections;
	
	//IAuthContext authContext;
	
	private File modelDirectory;
	
	private IMetaRepository metaRepository;
	
	private ILogger logger;
	
	private Map<String,String> nodeClasses;

	public JSONNavigationModel() {
		nodes = new HashMap<String, INode>();
		sections = new HashMap<String, INavigationSection>();
		nodeClasses = new HashMap<String, String>();
	}
	
	public File getModelDirectory(){
		return modelDirectory;
	}
	
	public void setModelDirectory(File d) throws IonException {
		modelDirectory = d;
		actualise();
	}
	
	public void setMetaRepository(IMetaRepository r){
		metaRepository = r;
	}
	
	public void setLogger(ILogger logger){
		this.logger = logger;
	}
	
	private ListOptions formListOptions(IStructMeta cm, Collection<StoredCondition> conditions, Collection<StoredSorting> sorting) throws Exception{
		Collection<FilterOption> cnd = new ArrayList<FilterOption>();
		Collection<Sorting>srt = new ArrayList<Sorting>();
		
		for (StoredCondition sc: conditions){
			cnd.add(new Condition(sc.property, ConditionType.fromInt(sc.operation.intValue()),StoredPropertyMeta.ParseValue(cm.PropertyMeta(sc.property).Type().getValue(), sc.value)));
		}
		
		for (StoredSorting ss: sorting){
			srt.add(new Sorting(ss.property,SortingMode.fromInt(ss.mode)));
		}
		
		ListOptions result = new ListOptions(cnd,srt);
		return result;
	}
	
	private Map<String, String[]> formPathChains(Collection<StoredPathChain> chains){
		Map<String, String[]> result = new HashMap<String, String[]>();
		for (StoredPathChain chain: chains){
			result.put(chain.class_name, chain.path);
		}
		return result;
	}
	
	private void processNodeLoadException(StoredNavNode node, Throwable e){
		if (logger != null){
			logger.Warning("Не удалось загрузить узел навигации " + node.code + ".", e);
		}
	}
	
	private void processSectionLoadException(StoredNavSection section, Throwable e){
		if (logger != null){
			logger.Warning("Не удалось загрузить секцию навигации " + section.name + ".", e);
		}
	}
	
	private Boolean validate(StoredNavNode node){
		JSONValidator v = new JSONValidator();
		String[] result = v.Validate(node);
		if (logger != null)
			for (String msg: result)
				logger.Warning(msg);
		return result.length == 0;
	}
	
	private Boolean validate(StoredNavSection section){
		JSONValidator v = new JSONValidator();
		String[] result = v.Validate(section);
		if (logger != null)
			for (String msg: result)
				logger.Warning(msg);
		return result.length == 0;
	}

	private boolean actualise() throws IonException {
		nodes.clear();
		sections.clear();
		if (modelDirectory.exists()){
			Gson gs = new GsonBuilder().serializeNulls().create();
			Reader rdr;
			StoredNavSection ss;
			INavigationSection nav_sec;
			StoredNavNode sd;
			INode node;
			
			File[] sectionMetafiles = modelDirectory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".section.json");
				}
			});
				
			try {
				for (File smf: sectionMetafiles){
					rdr = new InputStreamReader(new FileInputStream(smf),"UTF-8");
					ss = gs.fromJson(rdr, StoredNavSection.class);
					rdr.close();
					nav_sec = null;
					
					if (!validate(ss))
						processSectionLoadException(ss, new IonException("Секция навигации не прошла валидацию!"));
					else
						nav_sec = new NavigationSection(ss.name, ss.caption, new ArrayList<INode>(),NavigationSectionMode.fromInt(ss.mode),(ss.tags == null)?new HashSet<String>():new HashSet<String>(Arrays.asList(ss.tags)));
					if(nav_sec != null){						
						File[] nodesMetafiles = new File(modelDirectory, nav_sec.getName()).listFiles(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return name.toLowerCase().endsWith(".json");
							}
						});
												
						for (File cf: nodesMetafiles){
							rdr = new InputStreamReader(new FileInputStream(cf),"UTF-8");
							sd = gs.fromJson(rdr, StoredNavNode.class);
							rdr.close();
							node = null;

							if (!validate(sd)){
								processNodeLoadException(sd, new IonException("Узел навигации не прошел валидацию!"));
							} else {
								try {
									switch (NodeType.fromInt(sd.type)){
										case GROUP:node = new GroupNode((sd.orderNumber == null)?0:sd.orderNumber, sd.code, sd.caption, sd.hint);break;
										case HYPERLINK:node = new HyperlinkNode((sd.orderNumber == null)?0:sd.orderNumber, sd.code, sd.caption, sd.url, sd.hint);break;
										case CLASS:node = new ClassNode((sd.orderNumber == null)?0:sd.orderNumber, sd.code, sd.caption, sd.classname, sd.hint, formListOptions(metaRepository.Get(sd.classname), sd.conditions, sd.sorting),formPathChains(sd.pathChains));
												   nodeClasses.put(sd.classname, sd.code);	
												   break;
										case CONTAINER:node = new ContainerNode((sd.orderNumber == null)?0:sd.orderNumber,sd.code, sd.caption, sd.classname, sd.container, sd.collection, sd.hint, formListOptions(metaRepository.Get(sd.classname), sd.conditions, sd.sorting),formPathChains(sd.pathChains));break;
										default:node = new GroupNode((sd.orderNumber == null)?0:sd.orderNumber,sd.code, sd.caption, sd.hint);break;
									}
								} catch (NullPointerException e){
									processNodeLoadException(sd, e);
								}
							}
							if (node != null){
								if(!node.getCode().contains("."))
									((NavigationSection)nav_sec).addRootNode(node);
								nodes.put(node.getCode(), node);
							}
						}						

						sections.put(nav_sec.getName(), nav_sec);
					}
				}
			} catch(Exception e) {
				throw new IonException(e);
			}			
			
			String parentcode;
			int dotpos;			
			INode parent_node;
			for (INode n: nodes.values()){
				dotpos = n.getCode().lastIndexOf(".");
				if (dotpos > 0){
					parentcode = n.getCode().substring(0, dotpos);
					parent_node = nodes.get(parentcode);
					if (parent_node != null && parent_node.getType().equals(NodeType.GROUP)){
						((IGroupNode)parent_node).AddChild(n);
					}
				}				
			}
			return true;
		} else {
			throw new IonException("Директория "+ modelDirectory.getPath() +" не существует!");
		}	
	}

	@Override
	public Collection<INavigationSection> getNavigationSections() throws IonException {
		return sections.values();
	}

	@Override
	public INode getNode(String code) throws IonException {
		return nodes.get(code);
	}
	
	public String getNodeForClassname(String className){
		return nodeClasses.get(className);
	}

	@Override
	public INavigationSection getNavigationSection(String code)
																														 throws IonException {
		return sections.get(code);
	}
}
