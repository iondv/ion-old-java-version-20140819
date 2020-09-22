package ion.offline.adapters;

import ion.core.IonException;
import ion.framework.meta.plain.StoredClassMeta;
import ion.integration.core.UserCredentials;
import ion.offline.net.AuthResult;
import ion.offline.net.DataUnit;
import ion.offline.net.UserProfile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISitexSoapRequestor {

	public AuthResult[] authenticate(UserCredentials[] credentials) throws IonException;

	public UserProfile getProfile(String login, String pwd, Set<String> classNames, Map<String, Set<String>> classOrgs, Map<String, String> classTypes, Map<String, String[]> classNav) throws IonException;
	
	public Collection<DataUnit> requestSignatureTemplate(String className, String digestTplEncoding) throws IonException;
	
	public StoredClassMeta requestClass(String cn, Map<String, StoredClassMeta> classes, Map<String, String> classWsdl) throws IonException;
	
	
	public Collection<StoredClassMeta> requestClasses(Collection<String> names,
	                                                 // Date since,
	            																			Map<String, StoredClassMeta> classes,
	            																			Map<String, Map<String, String>> classAttrTabs,
	            																			Map<String, Map<String, String>> classAttrGroups,
	            																			Map<String, Map<String, Collection<String>>> classCollectionColumns,
	            																			Map<String, String> classWsdl,
	            																			Map<String, Integer> dictPermissions,
	            																			Map<String, Set<String>> dictionaries,
	            																			Map<String, String> classTypes,
	            																			List<UserProfile> profiles,
	            																			Map<String, Map<String, String>> collectionLinkClasses)
																									throws IonException;	
	
	public List<DataUnit> fetchData(ClassAssembler cm,
																	Map<String, Object> filter,
																	Map<String, StoredClassMeta> classes,
																	Integer pageCount) 
																								throws IonException;

	public List<DataUnit> fetchData(ClassAssembler cm,
																	Map<String, Object> filter,
																	Map<String, StoredClassMeta> classes,
																	Map<String, Set<String>> eager,
																	Integer pageCount)
																								 throws IonException;	

	public Map<String, String> push(DataUnit unit, String token, 
	                                Map<String, Set<String>> dictionaries, 
	                                Map<String, Map<String, String>> collectionLinkClasses)
																								 throws IonException;
	
	public String[] fetchResolutionDescDoctype(String doctypeOuid, 
	                                           Map<String, Set<String>> dictionaries, 
	                                           Map<String, Map<String, String>> collectionLinkClasses) 
	                                          		throws IonException;

	public void setUserWsdl(String userWsdl);

	public void setPpuUrl(String ppuUrl);
	
	public void setSignaturesFetchWsdl(String url);

	public void setSignaturesAttachWsdl(String url);
		
	public void setMaxPageCount(int maxPageCount);

	public void setEagerClasses(Set<String> ec);

	public void setSysLogin(String sysLogin);

	public void setSysToken(String sysToken);
	
	public void setSoapConnectionTimeout(int soapConnectionTimeout);
	
	public void setSoapReadTimeOut(int soapReadTimeOut);
	
	public void setDictionaryExcludeList(Set<String> dictionaryExcludeList);
	
	public void setDictionaryIncludeList(Set<String> dictionaryIncludeList);
	
	public void setSignatureAttachType(Map<String, String> attachTypes);
		
	public void setDebug(boolean v);
}
