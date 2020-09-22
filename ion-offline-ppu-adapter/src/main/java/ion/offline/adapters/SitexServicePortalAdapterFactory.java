package ion.offline.adapters;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import ion.core.IonException;
import ion.offline.security.HashProvider;
import ion.offline.util.IHashProvider;
import ion.offline.util.ITargetSystemAdapter;
import ion.offline.util.ITargetSystemAdapterFactory;

public class SitexServicePortalAdapterFactory implements ITargetSystemAdapterFactory {
	
	private String hashDir;
	
	private String digestTemplateHashDir;
	
	private String digestTemplatesUrl;

	private String viewsDir;
	
	private boolean loadDictionaries = false;
	
	private Set<String> dictionaryExcludeList = new HashSet<String>();
	
	private Set<String> dictionaryIncludeList = new HashSet<String>();
	
	private Set<String> digTplExcludeList = new HashSet<String>();
	
	private Set<String> digTplIncludeList = new HashSet<String>();	
	
	private Set<String> eagerClasses = new HashSet<String>();
	
	private Map<String, String> signatureAttachType = new HashMap<String, String>();	

	private IHashProvider hasher = new HashProvider();

	private Properties classTypeTitles;
	
	private boolean debug = false;
	
	private int version = 0;
	
	private String ppuUrl;
	
	private String userWsdl;
	
	private String mainWsdl;
	
	//private String digestTplWsdl;
	
	private String sysLogin;
	
	private String sysToken;
	
	private int maxPageCount = 15;
	
	private int connectTimeout = 5;
	
	private int readTimeout = 20;
	
	private List<String> initDictionaries = new LinkedList<String>();
	
	public List<String> getInitDictionaries() {
		return initDictionaries;
	}

	public void setInitDictionaries(List<String> initDictionaries) {
		this.initDictionaries = initDictionaries;
	}

	public void setSysLogin(String login) {
		sysLogin = login;
	}

	public void setSysToken(String token) {
		sysToken = token;
	}	
	
	public void setPpuUrl(String url){
		this.ppuUrl = url;
	}
	
	public String getUserWsdl() {
		return userWsdl;
	}

	public void setUserWsdl(String userWsdl) {
		this.userWsdl = userWsdl;
	}
	/*
	public String getDigestTplWsdl() {
		return digestTplWsdl;
	}

	public void setDigestTplWsdl(String digestTplWsdl) {
		this.digestTplWsdl = digestTplWsdl;
	}
*/
	public String getHashDir() {
		return hashDir;
	}

	public void setHashDir(String classHashFile) {
		this.hashDir = classHashFile;
	}

	public void setDigestTemplateHashDir(String digestTemplateHashDir) {
		this.digestTemplateHashDir = digestTemplateHashDir;
	}

	public void setDigestTemplatesUrl(String digestTemplatesUrl) {
		this.digestTemplatesUrl = digestTemplatesUrl;
	}

	public void setHasher(IHashProvider hasher) {
		this.hasher = hasher;
	}
	
	public void setMaxPageCount(int max){
		maxPageCount = max;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
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
	}

	public void setViewsDir(String value) {
		this.viewsDir = value;
  }
	
	public Set<String> getDictionaryExcludeList() {
		return dictionaryExcludeList;
	}

	public void setDictionaryExcludeList(Set<String> dictionaryExcludeList) {
		this.dictionaryExcludeList = dictionaryExcludeList;
	}
	

	public void setDictionaryIncludeList(Set<String> dictionaryIncludeList) {
		this.dictionaryIncludeList = dictionaryIncludeList;
	}

	public void setEagerClasses(Set<String> eagerClasses) {
		this.eagerClasses = eagerClasses;
	}
	
	public void addSignatureAttachType(String className, String type){
		signatureAttachType.put(className, type);
	}	

	@Override
	public ITargetSystemAdapter getAdapter() throws IonException {
		SitexServicePortalAdapter adapter = null;
		adapter = new SitexServicePortalAdapter(this.version);
		
		// TODO поубирать злоебучие костыли, реализовать абстрактный setup на уровне адаптера
		if (this.version > 0)
			adapter.setMainWsdl(mainWsdl);
		
		adapter.setSysLogin(sysLogin);
		adapter.setSysToken(sysToken);
		adapter.setPpuUrl(this.ppuUrl);
		adapter.setUserWsdl(this.userWsdl);
		//adapter.setDigestTemplatesUrl(this.digestTplWsdl);
		adapter.setEagerClasses(this.eagerClasses);
		adapter.setHashDir(this.hashDir);
		adapter.setHasher(this.hasher);
		
		adapter.setMaxPageCount(this.maxPageCount);
		adapter.setConnectTimeout(this.connectTimeout);
		adapter.setReadTimeout(this.readTimeout);
		
		adapter.setClassTypeTitles(this.classTypeTitles);
		adapter.setLoadDictionaries(this.loadDictionaries);
		adapter.setDebug(this.debug);
		adapter.setViewsDir(this.viewsDir);
		adapter.setDictionaryIncludeList(this.dictionaryIncludeList);
		adapter.setDictionaryExcludeList(this.dictionaryExcludeList);
		adapter.setDigTplExcludeList(this.digTplExcludeList);
		adapter.setDigTplIncludeList(this.digTplIncludeList);
		adapter.setDigestTemplateHashDir(this.digestTemplateHashDir);
		adapter.setDigestTemplatesUrl(this.digestTemplatesUrl);
		adapter.setSignatureAttachType(this.signatureAttachType);
		adapter.setInitDictionaries(this.initDictionaries);
		return adapter;
	}

	@Override
	public void setup(Properties properties) throws IonException {
		
		this.setSysLogin(properties.getProperty("adapter.sitex.login"));
		
		this.setSysToken(properties.getProperty("adapter.sitex.token"));
		
		if (properties.containsKey("adapter.sitex.version"))
			this.setVersion(Integer.parseInt(properties.getProperty("adapter.sitex.version")));
		
  	if (properties.containsKey("adapter.sitex.HashDir"))
  		this.setHashDir(properties.getProperty("adapter.sitex.HashDir"));
  	else
  		this.setHashDir(new File("hashes").getAbsolutePath());
  	
  	if (properties.containsKey("adapter.sitex.digestTplHashDir"))
  		this.setDigestTemplateHashDir(properties.getProperty("adapter.sitex.digestTplHashDir"));
  	else
  		this.setDigestTemplateHashDir(new File("digest-tpl-hashes").getAbsolutePath());
  	
  	if (properties.containsKey("adapter.sitex.maxPageCount"))
  		this.setMaxPageCount(Integer.parseInt(properties.getProperty("adapter.sitex.maxPageCount")));

  	if (properties.containsKey("adapter.sitex.connectTimeout"))
  		this.setConnectTimeout(Integer.parseInt(properties.getProperty("adapter.sitex.connectTimeout")));

  	if (properties.containsKey("adapter.sitex.readTimeout"))
  		this.setReadTimeout(Integer.parseInt(properties.getProperty("adapter.sitex.readTimeout")));
  	
  	if (properties.containsKey("adapter.loadDictionaries"))
  		this.setLoadDictionaries(Boolean.parseBoolean(properties.getProperty("adapter.loadDictionaries")));

  	if (properties.containsKey("adapter.sitex.debug"))
  		this.setDebug(Boolean.parseBoolean(properties.getProperty("adapter.sitex.debug")));    	
  	
 		this.setViewsDir(properties.getProperty("adapter.sitex.viewsDir", null));
  	
  	this.setPpuUrl(properties.getProperty("adapter.sitex.ppuUrl"));
  	
  	this.setDigestTemplatesUrl(properties.getProperty("adapter.sitex.digestTemplatesWsdl"));
  	
  	this.setUserWsdl(properties.getProperty("adapter.sitex.userWsdl"));
  	
  	if (properties.containsKey("adapter.sitex.mainWsdl"))
  		this.setMainWsdl(properties.getProperty("adapter.sitex.mainWsdl"));
  	
  	if (properties.containsKey("adapter.sitex.eagerClasses")){
  		Set<String> ec = new HashSet<String>();
  		String[] ec1 = properties.getProperty("adapter.sitex.eagerClasses").split("\\s+");
  		for (int i = 0; i < ec1.length; i++)
  			ec.add(ec1[i].trim());
  		this.setEagerClasses(ec);
  	}
  	
  	Properties ctt = new Properties();
  	for (Entry<Object, Object> p: properties.entrySet()){
  		if (p.getKey().toString().startsWith("adapter.sitex.classType."))
  			ctt.put(p.getKey().toString().replace("adapter.sitex.classType.", ""), p.getValue());
  	}
  	this.setClassTypeTitles(ctt);
  	
  	if (properties.containsKey("adapter.sitex.excludeDictionaries")){
  		Set<String> ex = new HashSet<String>();
  		String[] ex1 = properties.getProperty("adapter.sitex.excludeDictionaries").split("\\s+");
  		for (int i = 0; i < ex1.length; i++)
  			ex.add(ex1[i].trim());
  		this.setDictionaryExcludeList(ex);
  	}
  	
  	if (properties.containsKey("adapter.sitex.includeDictionaries")){
  		Set<String> in = new HashSet<String>();
  		String[] in1 = properties.getProperty("adapter.sitex.includeDictionaries").split("\\s+");
  		for (int i = 0; i < in1.length; i++)
  			in.add(in1[i].trim());
  		this.setDictionaryIncludeList(in);
  	}
  	
  	if (properties.containsKey("adapter.sitex.excludeDigTpl")){
  		Set<String> ex = new HashSet<String>();
  		String[] ex1 = properties.getProperty("adapter.sitex.excludeDigTpl").split("\\s+");
  		for (int i = 0; i < ex1.length; i++)
  			ex.add(ex1[i].trim());
  		this.setDigTplExcludeList(ex);
  	}
  	
  	if (properties.containsKey("adapter.sitex.includeDigTpl")){
  		Set<String> in = new HashSet<String>();
  		String[] in1 = properties.getProperty("adapter.sitex.includeDigTpl").split("\\s+");
  		for (int i = 0; i < in1.length; i++)
  			in.add(in1[i].trim());
  		this.setDigTplIncludeList(in);
  	} 	
  	  	
  	for (Entry<Object, Object> property: properties.entrySet())
  		if (property.getKey().toString().startsWith("cdsat."))
  			this.addSignatureAttachType(property.getKey().toString().substring(6), property.getValue().toString()); 
  	
  	if (properties.containsKey("adapter.sitex.initDictionaries")){
  		String[] in1 = properties.getProperty("adapter.sitex.initDictionaries").split("\\s+");
  		List<String> initDictionariesList = new LinkedList<String>();
  		for (String s : in1)
  			initDictionariesList.add(s);
  		this.setInitDictionaries(initDictionariesList);
  	}
	}

	public Set<String> getDigTplExcludeList() {
		return digTplExcludeList;
	}

	public void setDigTplExcludeList(Set<String> digTplExcludeList) {
		this.digTplExcludeList = digTplExcludeList;
	}

	public Set<String> getDigTplIncludeList() {
		return digTplIncludeList;
	}

	public void setDigTplIncludeList(Set<String> digTplIncludeList) {
		this.digTplIncludeList = digTplIncludeList;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getMainWsdl() {
		return mainWsdl;
	}

	public void setMainWsdl(String mainWsdl) {
		this.mainWsdl = mainWsdl;
	}	
}
