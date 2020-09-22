package ion.offline.adapters;

import ion.core.ConditionType;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredMatrixEntry;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSelectionProvider;
import ion.offline.net.ClassPermission;
import ion.offline.net.DataChange;
import ion.offline.net.DataUnit;
import ion.offline.net.UserProfile;

import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.zip.Inflater;
//import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SitexSoapRequestor2 extends BasicSitexSoapRequestor {
	
	private URL mainEndpoint = null;
	
	private String mainWsdl;
	
	public SitexSoapRequestor2() throws IonException {
		super();
	}
	
	protected URL getMainEndpoint() {
		if (mainEndpoint == null) {
			try {
				String[] params = getEndPointAndBinding(this.mainWsdl,
																								new HashMap<String, String>());

				this.mainEndpoint = new URL(params[0]);
			} catch (
							 XPathExpressionException | ParserConfigurationException
							 | SAXException | IOException e) {
				logger.Error("", e);
			}
		}
		return mainEndpoint;
	}
	
	private StoredClassMeta getClassFromCache(String cn, Map<String, StoredClassMeta> classes,
	                                          Map<String, String> classWsdl,
	                                          Map<String, Integer> dictPermissions,
	                                          Map<String, Map<String, String>> attrGroups) 
	                                          			throws UnsupportedOperationException, SOAPException, 
	                                          							IOException, XPathExpressionException {
		if (classes.containsKey(cn)){
			//logger.Info("class "+cn+" already loaded");
			return classes.get(cn);
		}
		if (_classes.containsKey(cn)) {
			StoredClassMeta result = _classes.get(cn);
			if (result != null){
				//logger.Info("class " + cn + " found in cache.");
				if (result.ancestor == null || result.ancestor.isEmpty())
					dictPermissions.put(result.name, ClassPermission.CREATE.getValue() | 
					                    ClassPermission.READ.getValue() | ClassPermission.UPDATE.getValue());
				else {
	  			if (!classes.containsKey(SitexServicePortalAdapter.CN_ROOT) && _classes.containsKey(SitexServicePortalAdapter.CN_ROOT))
	  				classes.put(SitexServicePortalAdapter.CN_ROOT, _classes.get(SitexServicePortalAdapter.CN_ROOT));
	  			
	  			if (!classes.containsKey(result.ancestor) && _classes.containsKey(result.ancestor))
	  				classes.put(result.ancestor, _classes.get(result.ancestor));					
				}
				
				classWsdl.put(cn, this.mainWsdl);
  			classes.put(cn, result);
  			
  			for (StoredPropertyMeta pm: result.properties){
  				if (pm.type.equals(MetaPropertyType.REFERENCE.getValue()) || pm.type.equals(MetaPropertyType.STRUCT.getValue())) {
  					if (pm.ref_class != null && !pm.ref_class.isEmpty() && !classes.containsKey(pm.ref_class))
  						getClassFromCache(pm.ref_class, classes, classWsdl, dictPermissions, attrGroups);
  				} else if (pm.type.equals(MetaPropertyType.COLLECTION.getValue())) {
  					if (pm.items_class != null && !pm.items_class.isEmpty() && !classes.containsKey(pm.items_class))
  						getClassFromCache(pm.items_class, classes, classWsdl, dictPermissions, attrGroups);
  				}
  			}
			} else
				logger.Info("Class " + cn + " is unresolvable!");
			return result;
		}
		return null;
	}
	
	private Integer parseTypeName(String tn) throws IonException {
		tn = tn.toLowerCase();
		if (tn.equals("string"))
			return MetaPropertyType.STRING.getValue();
		else if (tn.equals("integer"))
			return MetaPropertyType.INT.getValue();
		else if (tn.equals("text"))
			return MetaPropertyType.TEXT.getValue();
		else if (tn.equals("numeric"))
			return MetaPropertyType.DECIMAL.getValue();
		else if (tn.equals("float"))
			return MetaPropertyType.REAL.getValue();
		else if (tn.equals("datetime"))
			return MetaPropertyType.DATETIME.getValue();
		else if (tn.equals("boolean"))
			return MetaPropertyType.BOOLEAN.getValue();
		else if (tn.equals("objectslistreference"))
			return MetaPropertyType.REFERENCE.getValue();
		else if (tn.equals("backreference"))
			return MetaPropertyType.COLLECTION.getValue();
		else if (tn.equals("objectslist"))
			return MetaPropertyType.COLLECTION.getValue();
		else if (tn.equals("file"))
			return MetaPropertyType.FILE.getValue();
		
		return MetaPropertyType.STRING.getValue();
		//throw new IonException("Не удалось интерпретировать тип "+tn+"!");
	}
	
	@SuppressWarnings("unchecked")
	private void loadRqstMainSelectionLists(Collection<StoredMatrixEntry> rqstTypeSL, 
	                                        Collection<StoredMatrixEntry> pettOrgSL,
	                                        Map<String, StoredClassMeta> classes,
	                                        Collection<UserProfile> profiles,
	                                        Map<String, String> classWsdl) 
	                                        		throws IonException {
		logger.Info("loading rqstMain selection lists");
		try {
		String wsdl = getClassWsdl("rqstType");
		if (wsdl != null) {
			String[] params = getEndPointAndBinding(wsdl, new HashMap<String, String>());

			SOAPMessage request = messageFactory.createMessage();

			String operName = "getRqstType";

			Object[] rqstParams = getFetchSOAPMsgParams(wsdl, operName, "rqstType");
			
			if (rqstParams != null) {
				String SOAPAction = (String) rqstParams[0];
				String bodyNS = (String) rqstParams[1];
				operName = (String) rqstParams[5];

				String bodyPrefix = "umz";

				request.getSOAPPart().getEnvelope().addNamespaceDeclaration(bodyPrefix, bodyNS);
							
				request.getSOAPPart().getEnvelope().addNamespaceDeclaration("ppu", "http://sys.smev.ru/xsd/ppu");
							
				request.getMimeHeaders().addHeader("SOAPAction", SOAPAction);
				
				addSecurityTokenHeader(request, sysLogin, sysToken);

				SOAPBodyElement bl = request.getSOAPBody()
																		.addBodyElement(new QName(bodyNS, operName, bodyPrefix));
				
				SOAPElement el = bl.addChildElement(new QName(bodyNS,"CondOfrqstType",bodyPrefix));

				el = el.addChildElement(new QName(bodyNS,"rqstType",bodyPrefix));

	  		el = el.addChildElement(new QName(bodyNS, "senders", bodyPrefix));
	  		el = el.addChildElement(new QName("http://sys.smev.ru/xsd/ppu", "egOrganization", "ppu"));
	  		el = el.addChildElement(new QName("http://sys.smev.ru/xsd/ppu", "guid", "ppu"));
	  		
	  		Map<String, StoredMatrixEntry> rtME = new HashMap<String, StoredMatrixEntry>();
	  		Map<String, StoredMatrixEntry> poME = new HashMap<String, StoredMatrixEntry>();
	  		
	  		Map<String, Set<String>> orgParents = new HashMap<String, Set<String>>();
	    	
	    	StoredClassMeta orgClass = null;
	    	if (classes.containsKey("egOrganization"))
	    		orgClass = classes.get("egOrganization");
	    	else
	    		orgClass = requestClass("egOrganization", classes, classWsdl);
	    	
	    	ClassAssembler orgClassA = new ClassAssembler(orgClass, classes);
	    		
	    	Set<String> porgs = new HashSet<String>();
	    	
	    	for (UserProfile p: profiles) {
	    		if (p.properties.containsKey("organisations"))
	    			porgs.addAll((Set<String>)p.properties.get("organisations"));
	    	}
	    	
	    	for (String porg: porgs){
	    		Set<String> porgp = new HashSet<String>();
	    		porgp.add(porg);
	    		orgParents.put(porg, porgp);
	    	}
	    	
	  		Set<String> prnts = new LinkedHashSet<String>();
	  		prnts.addAll(porgs);
	  		
	    	while (!prnts.isEmpty()) {
	    		Map<String, Object> filter = new HashMap<String, Object>();
	    		filter.put("guid", prnts);
	    		List<DataUnit> orgitems = fetchData(orgClassA, filter, classes,new HashMap<String, Set<String>>(), 0);
	    		prnts.clear();
	    		for (DataUnit orgO: orgitems)
	    			if (orgO.data.containsKey("parent") && 
	    					orgO.data.get("parent") != null && 
	    					orgO.data.get("parent") instanceof Map){
	    				String guid = orgO.data.get("guid").toString();
	    				String forg = ((Map<String, Object>)orgO.data.get("parent")).get("guid").toString();
	    				prnts.add(forg);
	    				for (Set<String> opr: orgParents.values()){
	    					if (opr.contains(guid))
	    						opr.add(forg);
	    				}
	    			}
	    	}
	  				
				for (Map.Entry<String, Set<String>> orgpair: orgParents.entrySet()){
					for (String org: orgpair.getValue()){
  	  			el.setTextContent(org);
  	  			
    				request.saveChanges();
    					
    				SOAPMessage response = request(new URL(params[0]), request);
    					
    				Document responseDoc = response.getSOAPBody().getOwnerDocument();
    
    				XPath respParser = getXPath(responseDoc);
    
    				XPathExpression expr = respParser.compile("//*[local-name() = 'rqstType']");
    				NodeList classObjects = (NodeList) expr.evaluate(responseDoc, XPathConstants.NODESET);
    
    				for (int i = 0; i < classObjects.getLength(); i++) {
    					XPathExpression expr1 = respParser.compile("*[local-name() = 'guid']/text()");
    					String rtCode = expr1.evaluate(classObjects.item(i));
    					
    					expr1 = respParser.compile("*[local-name() = 'name']/text()");
    					String rtName = expr1.evaluate(classObjects.item(i));
    					
    					expr1 = respParser.compile("*[local-name() = 'provider']/*[local-name() = 'guid']/text()");
    					String providerGuid = expr1.evaluate(classObjects.item(i));
    					
    					expr1 = respParser.compile("*[local-name() = 'provider']/*[local-name() = 'name']/text()");
    					String providerName = expr1.evaluate(classObjects.item(i));
    					
    					StoredMatrixEntry me = null;
    					
    					if (!rtME.containsKey(orgpair.getKey())){
    						me = new StoredMatrixEntry();
    						me.conditions.add(new StoredCondition("rqstOrg", ConditionType.EQUAL.getValue(), orgpair.getKey()));
    						me.comment = "если отправитель = "+orgpair.getKey();
    						me.result = new LinkedList<StoredKeyValue>();
    						rtME.put(orgpair.getKey(), me);
    					} else
    						me = rtME.get(orgpair.getKey());
    					StoredKeyValue kv = new StoredKeyValue();
    					kv.key = rtCode;
    					kv.value = rtName;
    					me.result.add(kv);
    					
    					if (!poME.containsKey(orgpair.getKey()+"-"+rtCode)){
    						me = new StoredMatrixEntry();
    						me.conditions.add(new StoredCondition("rqstOrg", ConditionType.EQUAL.getValue(), orgpair.getKey()));
    						me.conditions.add(new StoredCondition("rqstTyp", ConditionType.EQUAL.getValue(), rtCode));
    						me.comment = "если отправитель = " + orgpair.getKey() + " и тип запроса = " + rtCode;
    						me.result = new LinkedList<StoredKeyValue>();
    						poME.put(orgpair.getKey()+"-"+rtCode, me);
    					} else
    						me = poME.get(orgpair.getKey()+"-"+rtCode);
    					
    					kv = new StoredKeyValue();
    					kv.key = providerGuid;
    					kv.value = providerName;
    					me.result.add(kv);    					
    				}
					}
				}
	  		
	  		rqstTypeSL.addAll(rtME.values());
	  		pettOrgSL.addAll(poME.values());
			}
		}
		} catch (DOMException | XPathExpressionException | SOAPException 
				| UnsupportedOperationException | IOException | ParserConfigurationException | SAXException e) {
			throw new IonException(e);
		}
	}
	
	@SuppressWarnings("serial")
	private void checkParentClass(StoredClassMeta cm,
																Map<String, StoredClassMeta> classes, Map<String, Map<String, String>> attrGroups) {
		if (!classes.containsKey(SitexServicePortalAdapter.CN_ROOT)) {
			StoredClassMeta r =new StoredClassMeta(false,
																			new LinkedList<String>(){{ add("guid"); }},
																			SitexServicePortalAdapter.CN_ROOT,
																			"Объект СМЭВ",
																			"guid",
																			new LinkedList<StoredPropertyMeta>() {
																				{
																					// add(new
																					// StoredPropertyMeta("guid","GUID",MetaPropertyType.GUID.getValue(),(short)36));
																				}
																			});
			classes.put(SitexServicePortalAdapter.CN_ROOT, r);
			_classes.put(SitexServicePortalAdapter.CN_ROOT, r);
		}

		if (cm.ancestor != null && !cm.ancestor.isEmpty()) {
			StoredClassMeta anc;
			if (!classes.containsKey(cm.ancestor)) {
				Collection<StoredPropertyMeta> ancprops = new LinkedList<StoredPropertyMeta>();
				boolean checkGroup = attrGroups.containsKey(cm.name);
				Map<String, String> groups = null;
				if (checkGroup)
					groups = attrGroups.get(cm.name);
				else
					groups = new HashMap<String, String>();
				
				for (StoredPropertyMeta pm : cm.properties) {
					if ((!checkGroup || !groups.containsKey(pm.name) || (
							!groups.get(pm.name).equals("Данные запроса")
							&&
							!groups.get(pm.name).equals("Данные ответа")
							)) && !(pm.name.equals("rqstTyp") || pm.name.equals("pettOrg")))
						ancprops.add(pm);
				}
				anc = new StoredClassMeta(false,
																	null,
																	cm.ancestor,
																	classCaptions.get(cm.ancestor).toString(),
																	"",
																	SitexServicePortalAdapter.CN_ROOT,
																	ancprops);
				_classes.put(anc.name, anc);
				classes.put(anc.name, anc);
			} else {
				anc = classes.get(cm.ancestor);
				Set<String> propnames = new HashSet<String>();
				for (StoredPropertyMeta pm : cm.properties)
					propnames.add(pm.name);

				List<StoredPropertyMeta> tmp = new ArrayList<>(anc.properties);

				for (StoredPropertyMeta apm : tmp)
					if (!propnames.contains(apm.name))
						anc.properties.remove(apm);

				tmp.clear();
			}
		}
	}
	
	public Collection<StoredClassMeta> requestClasses(Collection<String> names,
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
	            																																														 throws IonException {
		return requestClasses(names, classes, classAttrTabs, classAttrGroups, classCollectionColumns, classWsdl, 
		                      dictPermissions, dictionaries, classTypes, profiles, collectionLinkClasses, new HashSet<String>());
	}	
	
	public Collection<StoredClassMeta> requestClasses(Collection<String> names,
	            																			Map<String, StoredClassMeta> classes,
	            																			Map<String, Map<String, String>> classAttrTabs,
	            																			Map<String, Map<String, String>> classAttrGroups,
	            																			Map<String, Map<String, Collection<String>>> classCollectionColumns,
	            																			Map<String, String> classWsdl,
	            																			Map<String, Integer> dictPermissions,
	            																			Map<String, Set<String>> dictionaries,
	            																			Map<String, String> classTypes,
	            																			List<UserProfile> profiles,
	            																			Map<String, Map<String, String>> collectionLinkClasses,
	            																			Set<String> forceGuid)
	            																																														 throws IonException {
		Collection<StoredClassMeta> result = new LinkedList<StoredClassMeta>();
		String[] nms = names.toArray(new String[names.size()]);
		
		int rstart = 0;
		int rstop = 0;
		
		int retry = 11;
		
		while (rstart < nms.length){
			rstop = rstart + 10;
			if (rstop > nms.length)
				rstop = nms.length;
			try {
			result.addAll(_requestClasses(Arrays.asList(Arrays.copyOfRange(nms, rstart, rstop)), classes,
																		classAttrTabs,classAttrGroups,classCollectionColumns,classWsdl,dictPermissions,
																		dictionaries,classTypes,profiles,collectionLinkClasses,forceGuid));
			} catch (IOException e) {
				logger.Warning("Ошибка при запросе меты классов!", e);
				retry--;
				if (retry > 0){
					logger.Info("Повторная попытка № "+(10 - retry));
					continue;
				} else
					throw new IonException(e);
			}
			rstart = rstop;
		}
		return result;
	}
	
	
	@SuppressWarnings("serial")
	private Collection<StoredClassMeta> _requestClasses(Collection<String> names,
																			Map<String, StoredClassMeta> classes,
																			Map<String, Map<String, String>> classAttrTabs,
																			Map<String, Map<String, String>> classAttrGroups,
																			Map<String, Map<String, Collection<String>>> classCollectionColumns,																			
																			Map<String, String> classWsdl,
																			Map<String, Integer> dictPermissions,
																			Map<String, Set<String>> dictionaries,
																			Map<String, String> classTypes,
																			List<UserProfile> profiles,
																			Map<String, Map<String, String>> collectionLinkClasses,
																			Set<String> forceGuid)
																			throws IonException, IOException {
		Collection<StoredClassMeta> result = new LinkedList<StoredClassMeta>();
		
		URL endpoint = getMainEndpoint();
				
		try {
			StoredClassMeta cm;
			List<String> nms = new LinkedList<String>();
			for (String cn: names){
				cm = getClassFromCache(cn, classes, classWsdl, dictPermissions, classAttrGroups);
				if (cm == null)
					nms.add(cn);
				else
					result.add(cm);
			}
				
			if (nms.isEmpty())
				return result;
			
			logger.Info("loading class meta for classes "+StringUtils.join(nms, ", "));
			
  		SOAPMessage msg = messageFactory.createMessage();

  		String nsprefix = "get";
  		String nsuri = "http://xsd.smev.ru/ion/offline/GetGenericDefinitionRequest";
  		
  		msg.getSOAPPart().getEnvelope()
  			.addNamespaceDeclaration(nsprefix, nsuri);
  			
  		msg.getMimeHeaders().addHeader("SOAPAction", "GetGenericDefinitionRequest");
  			
  		addSecurityTokenHeader(msg, sysLogin, sysToken);
  
  		SOAPBodyElement bl = msg.getSOAPBody()
  																	.addBodyElement(new QName(nsuri, "GetGenericDefinitionRequest", nsprefix));
  			
  		SOAPElement el = bl.addChildElement(new QName(nsuri, "classType", nsprefix));
  			
  			// TODO надо выпилить этот фильтр и запрашивать только по списку имен
  		el.setTextContent("Other"); // Other | Dictionary  

  		
  		el = bl.addChildElement(new QName(nsuri, "classNames", nsprefix));
  		  		
  		for (String cn1: nms){
  			SOAPElement el1 = el.addChildElement(new QName(nsuri, "className", nsprefix));
  			el1.setTextContent(cn1);
  		}
  		
  		el = bl.addChildElement(new QName(nsuri, "zipResponse", nsprefix));
  		el.setTextContent("false");
  			/* TODO
  			if (since != null){
  				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  				el = bl.addChildElement(new QName("afterDate"));
  				el.setTextContent(format.format(since));
  			}
  			*/
  
  		msg.saveChanges();
  		SOAPMessage response = request(endpoint, msg);
  				
  		Document responseDoc = response.getSOAPBody().getOwnerDocument();
  
  		XPath respParser = getXPath(responseDoc);
  
  		XPathExpression expr = respParser.compile("//*[local-name() = 'genericObjDefinition']");
  		XPathExpression classNameExpr = respParser.compile("*[local-name() = 'className']/text()");
  		XPathExpression classCaptionExpr = respParser.compile("*[local-name() = 'classTitle']/text()");
  		
  		XPathExpression groupExpr = respParser.compile("*[local-name() = 'attrGroups']/*[local-name() = 'attrGroup']");
  		XPathExpression groupAttrExpr = respParser.compile("*[local-name() = 'code']|*[local-name() = 'title']|*[local-name() = 'parentCode']");
  		XPathExpression attrExpr = respParser.compile("*[local-name() = 'attributes']/*[local-name() = 'attribute']");

  		XPathExpression attrInListExpr = respParser.compile("*[local-name() = 'inlist']/text()");
  		XPathExpression attrOnFormExpr = respParser.compile("*[local-name() = 'inform']/text()");
  		  		
  		XPathExpression attrNumExpr = respParser.compile("*[local-name() = 'num']/text()");
  		XPathExpression attrNameExpr = respParser.compile("*[local-name() = 'name']/text()");
  		XPathExpression attrTitleExpr = respParser.compile("*[local-name() = 'title']/text()");
  		XPathExpression attrTypeExpr = respParser.compile("*[local-name() = 'type']/*[local-name() = 'logicName']/text()");
  		XPathExpression attrGroupExpr = respParser.compile("*[local-name() = 'group']/*[local-name() = 'code']/text()");
  		XPathExpression attrLengthExpr = respParser.compile("*[local-name() = 'length']/text()");
  		XPathExpression attrIsKeyExpr = respParser.compile("*[local-name() = 'primaryKey']/text()");
  		XPathExpression attrIsSemanticExpr = respParser.compile("*[local-name() = 'header']/text()");
  		XPathExpression attrGridAttrsExpr = respParser.compile("*[local-name() = 'gridAttrList']/*[local-name() = 'gridAttr']");
  		//XPathExpression attrAiExpr = respParser.compile("*[local-name() = 'autoincrement']/text()");
  		XPathExpression attrNotNullExpr = respParser.compile("*[local-name() = 'mandatory']/text()");
  		XPathExpression attrReadOnlyExpr = respParser.compile("*[local-name() = 'readonly']/text()");
  		XPathExpression attrRefAttrExpr = respParser.compile("*[local-name() = 'refAttr']/text()");
  		XPathExpression attrRefClassExpr = respParser.compile("*[local-name() = 'refClass']/*[local-name() = 'code']/text()");
  		//XPathExpression attrDefaultExpr = respParser.compile("*[local-name() = 'defValue']/*[local-name() = 'value']|*[local-name() = 'defValue']/text()/*[local-name() = 'refClass']/*[local-name() = 'identifier']/text()");
  		XPathExpression attrSelectionExpr = respParser.compile("*[local-name() = 'enums']/*[local-name() = 'enum']");
  		
  		NodeList classNodes = (NodeList) expr.evaluate(responseDoc, XPathConstants.NODESET);
  
  		Set<String> refClasses = new HashSet<String>();
  		logger.Info("ref load list length is "+refClasses.size());
  		for (int i = 0; i < classNodes.getLength(); i++) {
  			Node c = classNodes.item(i);
  			
  			String cname = (String)classNameExpr.evaluate(c, XPathConstants.STRING);

  			cm = new StoredClassMeta(false,
																 null,
																 cname,
																 (String)classCaptionExpr.evaluate(c, XPathConstants.STRING),
																 null,
																 null,
																 new LinkedList<StoredPropertyMeta>());
  			
  			_classes.put(cname, cm);
  			classWsdl.put(cname, this.mainWsdl);
  			classes.put(cname, cm);
  			
  			String classType = classTypes.containsKey(cname)?classTypes.get(cname):"";
  			
  			String ancestor = classType.equals(CT_FEDERAL) ? SitexServicePortalAdapter.CN_FEDERAL
  			                                     											: (classType.equals(CT_REGIONAL) ? SitexServicePortalAdapter.CN_REGIONAL
  			                                     											: (classType.equals(CT_PETITION) ? SitexServicePortalAdapter.CN_PETITION: ""));			
  			
				cm.ancestor = ancestor;
				
				Map<String, String[]> groups = new HashMap<String, String[]>();
				Map<String,String[]> tmp1 = new HashMap<String, String[]>();
				
				NodeList groupNodes = (NodeList) groupExpr.evaluate(c, XPathConstants.NODESET);
				for (int j = 0; j < groupNodes.getLength(); j++) {
					NodeList groupAttrs = (NodeList) groupAttrExpr.evaluate(groupNodes.item(j), XPathConstants.NODESET);
					String gcode = "";
					String gtitle = "";
					String gparent = "";
					for (int k = 0; k < groupAttrs.getLength(); k++) {
						Node ga = groupAttrs.item(k);
						if (ga.getLocalName().equals("code"))
							gcode = ga.getTextContent();
						else if (ga.getLocalName().equals("title"))
							gtitle = ga.getTextContent();
						else if (ga.getLocalName().equals("parentCode"))
							gparent = ga.getTextContent();
					}
					
					groups.put(gcode, new String[]{gcode, gtitle, gparent});
					tmp1.put(gcode, new String[]{gcode, gtitle, gparent});
				}
				
				List<String[]> tmp = new LinkedList<String[]>();  				
				tmp.addAll(groups.values());

				for (String[] g: tmp){
					String path = "";
					String[] p = g;
					do {
							path = p[1] + ((!path.isEmpty())?"|":"") + path;
							
	  					String pcode = "";
							
	  					if (p[1].equals("Данные запроса") || 
									p[1].equals("Данные ответа") || 
									p[1].equals("Общая информация"))
								pcode = "";
							else {
								if (p[2].isEmpty())
										path = "Общая информация|" + path;		
								pcode = p[2];
							}
							
							p = null;
							if (tmp1.containsKey(pcode))
								p = tmp1.get(pcode);
					} while (p != null);
					
					g[1] = path;
					g[2] = "";
					groups.put(g[0], g);
				}
				
				Collection<StoredMatrixEntry> rqstTypeSL = null;
				Collection<StoredMatrixEntry> pettOrgSL = null;  				
				
				NodeList attrNodes = (NodeList) attrExpr.evaluate(c, XPathConstants.NODESET);
				
				boolean hasGuid = false;
				
				String semantic = "";
				
				for (int j = 0; j < attrNodes.getLength(); j++) {
					Node a = attrNodes.item(j);

					String aname = (String)attrNameExpr.evaluate(a, XPathConstants.STRING);
					
					if (aname.equals("guid"))
						hasGuid = true;
					
					if ("true".equals((String)attrIsKeyExpr.evaluate(a, XPathConstants.STRING))){
						cm.key = new LinkedList<String>();
						cm.key.add(aname);
					}
				
	  			boolean visible = "true".equals((String)attrInListExpr.evaluate(a, XPathConstants.STRING)) || 
	  					!"NONE".equals((String)attrOnFormExpr.evaluate(a, XPathConstants.STRING));
					
	  			if (!visible && !aname.equals(cm.key) && !aname.equals("guid"))
	  				continue;  					
					
					if (!aname.matches("[a-zA-Z][a-zA-Z0-9_\\$]*") ||
							aname.equals("systemClass") || aname.equals("preparedData") || aname.equals("sedECP") ||
							(aname.equals("history") && ancestor.equals(SitexServicePortalAdapter.CN_PETITION)))
						continue;

					String arefclass = (String)attrRefClassExpr.evaluate(a, XPathConstants.STRING);
					
					if (arefclass.startsWith("SX") || arefclass.equals("sedECP"))
						continue;
			
					if (!ancestor.isEmpty() && aname.equals("guid"))
						continue;		
		
					//---------- Start: Формируем табы и группы для атрибута
					
					String agroup = (String)attrGroupExpr.evaluate(a, XPathConstants.STRING);
					
					String tab = "Общая информация";
					
					if (agroup != null && !agroup.isEmpty() && groups.containsKey(agroup)){
						String[] g = groups.get(agroup);
						
						if (g[1].contains("|")){
							tab = g[1].substring(0, g[1].indexOf("|"));
							String gpath = g[1].substring(g[1].indexOf("|") + 1);
							if (!classAttrGroups.containsKey(cname))
								classAttrGroups.put(cname, new HashMap<String, String>());
							classAttrGroups.get(cname).put(aname, gpath);
						} else {
							tab = g[1];
						}
					}
					
					if (!classAttrTabs.containsKey(cname))
						classAttrTabs.put(cname, new HashMap<String, String>());
					classAttrTabs.get(cname).put(aname, tab);
					
					//---------- Stop: Формируем табы и группы для атрибута
					
					if ((aname.equals("overdue") || aname.equals("timeStamp") || aname.equals("createDate")) && !ancestor.isEmpty())
						continue;
					
					if ("true".equals((String)attrIsSemanticExpr.evaluate(a, XPathConstants.STRING)))
						semantic = semantic + (semantic.isEmpty()?"":"| |") + aname;
					
					
					int anum = 0;
					try {
						anum = Integer.parseInt((String)attrNumExpr.evaluate(a, XPathConstants.STRING));
					} catch (Exception e) {
						
					}
					
					String atitle = (String)attrTitleExpr.evaluate(a, XPathConstants.STRING);
					
					int	atype = parseTypeName((String)attrTypeExpr.evaluate(a, XPathConstants.STRING));
					
					if (atype == MetaPropertyType.STRING.getValue() && aname.equals("guid"))
						atype = MetaPropertyType.GUID.getValue();
					
					Short alength = null;
					try {
						alength = Short.parseShort((String)attrLengthExpr.evaluate(a, XPathConstants.STRING));
					} catch (Exception e){
						
					}
					  					
					boolean aai = aname.equals("guid");
					//boolean aai = "true".equals((String)attrAiExpr.evaluate(a, XPathConstants.STRING));
					
					boolean anotnull = "true".equals((String)attrNotNullExpr.evaluate(a, XPathConstants.STRING));
					if (tab.equals("Общая информация"))
						anotnull = false;
					
					boolean areadonly = "ALL".equals((String)attrReadOnlyExpr.evaluate(a, XPathConstants.STRING));
					
					String abackref = (String)attrRefAttrExpr.evaluate(a, XPathConstants.STRING);
					  					
					if ((atype == MetaPropertyType.REFERENCE.getValue() || atype == MetaPropertyType.COLLECTION.getValue())){
  					if (arefclass != null && !arefclass.isEmpty()){
  						getClassFromCache(arefclass, classes, classWsdl, dictPermissions, classAttrGroups);
  						if (!classes.containsKey(arefclass)){
  							//logger.Info("adding class " + arefclass + " to load list for ref property "+cname + "." + aname);
  							refClasses.add(arefclass);
  							if (atype == MetaPropertyType.COLLECTION.getValue() &&
  									(classAttrTabs.containsKey(cname) && classAttrTabs.get(cname).containsKey(aname)) &&
  									(
  										(
  											classAttrTabs.get(cname).get(aname).equals("Данные запроса") 
  											&& 
  											(ancestor.equals(SitexServicePortalAdapter.CN_FEDERAL) || ancestor.equals(SitexServicePortalAdapter.CN_REGIONAL))
  										) 
  										|| 
  										(
  											classAttrTabs.get(cname).get(aname).equals("Данные ответа") 
  											&& 
  											(ancestor.equals(SitexServicePortalAdapter.CN_PETITION) || ancestor.equals(SitexServicePortalAdapter.CN_REGIONAL))
  										)
  									)
  								)  {
  								forceGuid.add(arefclass);
  							}
  						}
  					} else {
  						logger.Warning("ссылочный атрибут "+aname+" не включен в класс "+cname+" так как у него не указан класс ссылки!");
  						continue;
  					}
					}
					
					NodeList gridAttrNodes = (NodeList)attrGridAttrsExpr.evaluate(a, XPathConstants.NODESET);
					if (gridAttrNodes.getLength() > 0){
						if (!classCollectionColumns.containsKey(cm.name))
							classCollectionColumns.put(cm.name, new HashMap<String, Collection<String>>());
						Map<String, Collection<String>> collColumns = classCollectionColumns.get(cm.name);
						if (!collColumns.containsKey(aname))
							collColumns.put(aname, new LinkedList<String>());
						Collection<String> columns = collColumns.get(aname);
						for (int k = 0; k < gridAttrNodes.getLength(); k++) {
							Node gridAttr = gridAttrNodes.item(k);
							columns.add(gridAttr.getAttributes().getNamedItem("name").getNodeValue());
						}
					}
					// String adefault = (String)attrDefaultExpr.evaluate(a, XPathConstants.STRING);
					String adefault = null;
					
					NodeList aselection = (NodeList)attrSelectionExpr.evaluate(a, XPathConstants.NODESET);
					
					StoredPropertyMeta pm = new StoredPropertyMeta(anum, aname, atitle, atype,
					                   	                    			alength, null, !anotnull, 
					                  	                    			areadonly, false, false, aai,"", adefault,
					                  	                    			(atype == MetaPropertyType.REFERENCE.getValue())?arefclass:null, 
					                  	                    			(atype == MetaPropertyType.COLLECTION.getValue())?arefclass:null, 
					                  	                    			abackref, null, null,null,null,false,false,null,null);
					
					if (cname.equals("rqstMain")) {
						if (aname.equals("rqstTyp")) {
							if (rqstTypeSL == null) {
								rqstTypeSL = new LinkedList<StoredMatrixEntry>();
								pettOrgSL = new LinkedList<StoredMatrixEntry>();
								loadRqstMainSelectionLists(rqstTypeSL, pettOrgSL, classes, profiles, classWsdl);
							}
							pm.selection_provider = new StoredSelectionProvider(rqstTypeSL);									
						} else if (aname.equals("pettOrg")) {
							if (pettOrgSL == null) {
								rqstTypeSL = new LinkedList<StoredMatrixEntry>();
								pettOrgSL = new LinkedList<StoredMatrixEntry>();
								loadRqstMainSelectionLists(rqstTypeSL, pettOrgSL, classes, profiles, classWsdl);
							}
							pm.selection_provider = new StoredSelectionProvider(pettOrgSL);
						}
					} else if (aselection.getLength() > 0) {
						Map<String, String> selection = new LinkedHashMap<String, String>();
						for (int k = 0; k < aselection.getLength(); k++)
							selection.put(aselection.item(k).getAttributes().getNamedItem("code").getNodeValue(), aselection.item(k).getTextContent());
						pm.selection_provider = new StoredSelectionProvider(selection);
					}
					
					if(eagerClasses.contains(arefclass))
						pm.eager_loading = true;  					
					
					cm.properties.add(pm);
				}
				
				if (!semantic.isEmpty())
					cm.semantic = semantic;
				
				
				
				if (
						hasGuid && 
						(
								(cm.ancestor != null && !cm.ancestor.isEmpty()) || 
								cm.name.equals("egOrganization") || 
								cm.name.equals("SD_Employee") ||
								forceGuid.contains(cm.name)								
						))
					cm.key = new LinkedList<String>(){{ add("guid"); }};
				
				if ((cm.key != null) && !cm.key.equals("guid"))
					cm.ancestor = null;
				
				if (cm.ancestor != null && !cm.ancestor.isEmpty())
					cm.key = null;
				
				if (((cm.key == null) || cm.key.isEmpty()) && ((cm.ancestor == null) || cm.ancestor.isEmpty())){
					cm.is_struct = true;
					logger.Info("Class " + cm.name + " has no key nor ancestor and was treated as struct.");
				}
			
  			if (cm.ancestor == null || cm.ancestor.isEmpty()){
  				int dperm = 0;
  				if (dictPermissions.containsKey(cm.name))
  					dperm = dictPermissions.get(cm.name);
  				dperm = dperm | ClassPermission.READ.getValue() | ClassPermission.CREATE.getValue();
  				dictPermissions.put(cm.name, dperm);
  			}  			
  			
  			result.add(cm);
  		}
  		
  		if (!refClasses.isEmpty()) {
  			
  			requestClasses(refClasses, classes, classAttrTabs, classAttrGroups, classCollectionColumns, classWsdl, 
  			               dictPermissions, dictionaries, classTypes, profiles, collectionLinkClasses, forceGuid);
  			
  			for (String rc: refClasses)
  				if (!classes.containsKey(rc))
  					logger.Error("Класс " + rc + " не был загружен для ссылочного атрибута!");
  		}
  		
  		for (StoredClassMeta cm1: result){
  			for (StoredPropertyMeta pm: cm1.properties)
  				if (pm.type == MetaPropertyType.REFERENCE.getValue()){
  					StoredClassMeta rc = _classes.get(pm.ref_class);
  					if (rc != null && rc.is_struct)
  						pm.type = MetaPropertyType.STRUCT.getValue();
  				}
  			
  			if (cm1.ancestor != null && !cm1.ancestor.isEmpty()){
  				checkParentClass(cm1, classes, classAttrTabs);
  				if (cm1.ancestor.equals(SitexServicePortalAdapter.CN_PETITION)){
  		  		Map<String, String> decisions = fetchResolutionDesc(cm1.name);
  		  		cm1.properties.add(new StoredPropertyMeta(5,
  		  		                                           "offlnResolutionDecision",
  		  																								"Решение",
  		  																								MetaPropertyType.INT.getValue(),
  		  																								null,null,
  		  																								true,false,false,false,false,"",
  		  																								null,null,null,null,null,null,
  		  																								null,null,new StoredSelectionProvider(decisions),false,false,null,null));
  				}
  			}  			
  		}
  		
  		for (String nm: names){
  			if (!classes.containsKey(nm))
  				logger.Error("Класс " + nm + " не был найден!");
  		}
  		
		} catch (DOMException | XPathExpressionException | SOAPException | UnsupportedOperationException e) {
			throw new IonException(e);
		}
		return result;
	}
	
	private DataUnit parseItem(Node itemNode, ClassAssembler cm, XPath respParser, Map<String, Set<String>> eager, Map<String, StoredClassMeta> classes) throws XPathExpressionException, IonException{
		Map<String, Object> data = new HashMap<String, Object>();
		XPathExpression expr = respParser.compile("*[local-name() = 'identifier']");
		data.put("__systemClass", cm.name);
		String id = null;
		Node idNode = (Node)expr.evaluate(itemNode, XPathConstants.NODE);
		if (idNode != null){
			id = idNode.getTextContent();
			data.put(idNode.getAttributes().getNamedItem("name").getNodeValue(), id);
		}
		expr = respParser.compile("*[local-name() = 'attributes']/*[local-name() = 'attr']");
		XPathExpression collItemExpr = respParser.compile("*[local-name() = 'genericItems']/*[local-name() = 'genericItem']");
		XPathExpression refItemExpr = respParser.compile("*[local-name() = 'genericItem']");
		Map<String, String> classWsdl = new HashMap<String, String>();
		
		NodeList attrs = (NodeList)expr.evaluate(itemNode, XPathConstants.NODESET);
		for (int i = 0; i < attrs.getLength(); i++){
			Node a = attrs.item(i);
			String nm = a.getAttributes().getNamedItem("name").getNodeValue();
			StoredPropertyMeta pm = cm.properties.get(nm);
			DataUnit tmp = null;
			if (pm != null) {
				if (pm.type == MetaPropertyType.COLLECTION.getValue()){
					Collection<Map<String, Object>> col = new LinkedList<Map<String, Object>>();
					NodeList colItems = (NodeList)collItemExpr.evaluate(a, XPathConstants.NODESET);
					StoredClassMeta icm = requestClass(pm.items_class, classes, classWsdl);
					ClassAssembler ica = new ClassAssembler(icm, classes);
					for (int j  = 0; j < colItems.getLength(); j++){
						tmp = parseItem(colItems.item(j), ica, respParser, /*eager*/ new HashMap<String, Set<String>>(), classes);
						col.add(tmp.data);
					}
					data.put(nm,  col);
				} else if (pm.type == MetaPropertyType.REFERENCE.getValue()){
					if (eager.containsKey(cm.name) && eager.get(cm.name).contains(nm)){
						Node tmpNode = (Node)refItemExpr.evaluate(a, XPathConstants.NODE);
						StoredClassMeta rcm = requestClass(pm.ref_class, classes, classWsdl);
						if (tmpNode != null){
							tmp = parseItem(tmpNode, new ClassAssembler(rcm, classes), respParser, /*eager*/ new HashMap<String, Set<String>>(), classes);
							data.put(nm, tmp.data);
						} else {
							final String rid = a.getTextContent();
							if (rid != null && !rid.isEmpty()){
  							@SuppressWarnings("serial")
  							Collection<DataUnit> refs = fetchData(new ClassAssembler(rcm, classes), new HashMap<String, Object>(){{
  								put("ouid", rid);
  							}}, classes, 1);
  							for (DataUnit r: refs){
  								data.put(nm, r.data);
  								break;
  							}
							} else
								data.put(nm, null);
						}
					} else
						data.put(nm, a.getTextContent());
				} else {
					if (nm.equals(cm.key))
						id = a.getTextContent();
					data.put(nm, a.getTextContent());
				}
			}
		}
		return new DataUnit(id, cm.name, data);
	}

	@Override
	public List<DataUnit> fetchData(ClassAssembler cm,
																	Map<String, Object> filter,
																	Map<String, StoredClassMeta> classes,
																	Map<String, Set<String>> eager,
																	Integer pageCount)
																										throws IonException {
		List<DataUnit> result = new LinkedList<DataUnit>();
		
		URL endpoint = getMainEndpoint();
		
		try {

  		SOAPMessage msg = messageFactory.createMessage();
  
  		String nsprefix = "get";
  		String nsuri = "http://xsd.smev.ru/ion/offline/GetGenericItemsRequest";

  		msg.getSOAPPart().getEnvelope()
  			.addNamespaceDeclaration(nsprefix, nsuri);
  			
  		msg.getMimeHeaders().addHeader("SOAPAction", "GetGenericItemsRequest");
  			
  		addSecurityTokenHeader(msg, sysLogin, sysToken);
  
  		SOAPBodyElement bl = msg.getSOAPBody()
  																	.addBodyElement(new QName(nsuri, "GetGenericItemsRequest", nsprefix));
  			
  		SOAPElement el = bl.addChildElement(new QName(nsuri, "className", nsprefix));
  			
  		el.setTextContent(cm.name);  
  		
  		el = bl.addChildElement(new QName(nsuri, "getFileAttrs", nsprefix));
  		
  		el.setTextContent("true");
  		
  		el = bl.addChildElement(new QName(nsuri, "getListAttrs", nsprefix));
  		
  		el.setTextContent("true");
  		
  		el = bl.addChildElement(new QName(nsuri, "selectAttrs", nsprefix));
  		el = el.addChildElement(new QName(nsuri, "attrNames", nsprefix));
  		
  		String anames = "";
  		for (StoredPropertyMeta pm: cm.properties.values()){
  			if (!pm.name.startsWith("offln")) {
  				anames += (anames.isEmpty()?"":",") + pm.name;
  				if (pm.type == MetaPropertyType.REFERENCE.getValue() && 
  						pm.ref_class != null && classes.containsKey(pm.ref_class)){
  					StoredClassMeta rcm = classes.get(pm.ref_class);
  					if (rcm.key != null && rcm.key.equals("guid")){
  						if (!eager.containsKey(cm.name))
  							eager.put(cm.name, new HashSet<String>());
  						eager.get(cm.name).add(pm.name);
  					}
  				}  					
  			}
  		}
  		el.setTextContent(anames);
  		
			SOAPElement el1 = bl.addChildElement(new QName(nsuri, "preloadLinkAttrs", nsprefix));
  		
  		if (eager.containsKey(cm.name)){
  			el = el1.addChildElement(new QName(nsuri, "attrNames", nsprefix));

  			anames = "";
  			for (String aname: eager.get(cm.name))
  				if (!aname.startsWith("offln")){
  					anames += (anames.isEmpty()?"":",") + aname;
  				}
  			
  			el.setTextContent(anames);
  		}
  		
  		el = el1.addChildElement(new QName(nsuri, "attrCollection", nsprefix));
  		el.setTextContent("ALL_ATTRS");
  		el = el1.addChildElement(new QName(nsuri, "getFileAttrs", nsprefix));
  		el.setTextContent("true");
  		el = el1.addChildElement(new QName(nsuri, "getListAttrs", nsprefix));
  		el.setTextContent("true");
  		
  		
  		if (!filter.isEmpty()){
  			el = bl.addChildElement(new QName(nsuri, "selectCondition", nsprefix));
  			for (Map.Entry<String, Object> kv: filter.entrySet()){
  				if (cm.properties.containsKey(kv.getKey()) && !kv.getKey().equals("timeStamp")){
  					StoredPropertyMeta pm = cm.properties.get(kv.getKey());
    				SOAPElement a = el.addChildElement(new QName(nsuri, "attr", nsprefix));
    				a.setAttribute("name", kv.getKey());
    				if (pm.type == MetaPropertyType.COLLECTION.getValue())
    					a.setAttribute("criteria", "contains");
    				else {
    					if (kv.getValue() instanceof Collection)
    						a.setAttribute("criteria", "in");
    					else
    						a.setAttribute("criteria", "eq");
    				}
    				a = a.addChildElement(new QName(nsuri, "values", nsprefix));
    				if (kv.getValue() instanceof Collection){
    					@SuppressWarnings("unchecked")
							Collection<Object> tmp = (Collection<Object>)kv.getValue();
    					for (Object v: tmp) {
      					SOAPElement va = a.addChildElement(new QName(nsuri, "value", nsprefix));
      					va.setTextContent(v.toString());    						
    					}
    				} else {
    					a = a.addChildElement(new QName(nsuri, "value", nsprefix));
    					a.setTextContent(kv.getValue().toString());
    				}
  				}
  			}
  		}
  		
  		if (cm.properties.containsKey("timeStamp")){
  			el = bl.addChildElement(new QName(nsuri, "orderBy", nsprefix));
  			el.setAttribute("attrName", "timeStamp");
  		}

  		if (filter.containsKey("timeStamp")){
  			el = bl.addChildElement(new QName(nsuri, "afterDate", nsprefix));
  			el.setTextContent(filter.get("timeStamp").toString());
  		}
  		
  		SOAPElement limitNode = bl.addChildElement(new QName(nsuri, "limit", nsprefix));
			limitNode.setTextContent(String.valueOf(50));  		
  		SOAPElement offsetNode = bl.addChildElement(new QName(nsuri, "offset", nsprefix));
  			
  		el = bl.addChildElement(new QName(nsuri, "zipResponse", nsprefix));
  		el.setTextContent("true");
  
			SOAPMessage response;
			if(pageCount == null)
				pageCount = maxPageCount;
  		double fetched = 0;
  		int page = 0;
  		
			do {  		
        logger.Debug("loading page " + (page + 1) + " of list for class "+cm.name);
				offsetNode.setTextContent(String.valueOf(50 * page));
				
				msg.saveChanges();
				
				response = request(endpoint, msg);
  				
				Document responseDoc = response.getSOAPBody().getOwnerDocument();
  
				XPath respParser = getXPath(responseDoc);
	  		XPathExpression zip = respParser.compile("//*[local-name() = 'zipResponse']/text()");
	  		String b64 = (String)zip.evaluate(responseDoc, XPathConstants.STRING);
	  		if (b64 != null){
	  			String res = "";
	      	byte[] zipData = Base64.decodeBase64(b64);
	      	//ByteArrayOutputStream outputStream = new ByteArrayOutputStream(zipData.length);
  	  		ZipInputStream archive_stream = null;
  	      try {
  	      	archive_stream = new ZipInputStream(new ByteArrayInputStream(zipData));
  	      	while (archive_stream.getNextEntry() != null){
  	      		res += IOUtils.toString(archive_stream, "utf-8");
  	      	}
  	      } catch (Exception e) {
  	      	throw new IonException(e);  	      	
  	      } finally {
  	        IOUtils.closeQuietly(archive_stream);
  	      }
/*
  	      try {
  	      	Inflater inflater = new Inflater();   
  	      	inflater.setInput(zipData);  
  	      	byte[] buffer = new byte[1024];  
  	      	
  	      	while (!inflater.finished()) {
  	      		int count = inflater.inflate(buffer);  
  	      		outputStream.write(buffer, 0, count);  
  	      	}
  	        res = new String(outputStream.toByteArray(), "utf-8");
  	      } catch (Exception e) {
  	      	throw new IonException(e);
  	      } finally {
  	      	outputStream.close();
  	      }
  	*/      
  	      fetched = 0;
  	      
  	  		if (!res.isEmpty()){
  	  			if (this.debug){
  	  				logger.Debug("fetch result");
  	  				logger.Debug(res);
  	  			}
  	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  	        DocumentBuilder builder = factory.newDocumentBuilder();
  	        responseDoc = builder.parse(new InputSource(new StringReader(res)));
  	        
  	        respParser = getXPath(responseDoc);
  	        XPathExpression itemExpr = respParser.compile("/*[local-name() = 'genericItems']/*[local-name() = 'genericItem']");
  	        NodeList items = (NodeList)itemExpr.evaluate(responseDoc, XPathConstants.NODESET);
  	        fetched = items.getLength();
  	        
  	        logger.Debug(fetched + " items loaded for page " + (page+1) + " of list for class "+cm.name);  	        
  	        
  	        for (int i = 0; i < fetched; i++){
  	        	Node item = items.item(i);
  	        	DataUnit du = parseItem(item, cm, respParser, eager, classes);
  	        	if (du != null)
  	        		result.add(du);
  	        }
  	  		}
	  		}
	  		page++;
			} while ((fetched == 50) && (pageCount == 0 || page < pageCount));
  		
		} catch (DOMException | SOAPException | UnsupportedOperationException 
				| IOException | XPathExpressionException | ParserConfigurationException | SAXException e) {
			throw new IonException(e);
		}
    logger.Info(result.size() + " items of class " + cm.name + " loaded");  	        

		return result;
	}

	@Override
	public Map<String, String> push(DataUnit unit,
																	String token,
																	Map<String, Set<String>> dictionaries,
																	Map<String, Map<String, String>> collectionLinkClasses)
																																												 throws IonException {
		Map<String, String> result = new HashMap<String, String>();
		if (unit instanceof DataChange){
  		try {
				DataChange change = (DataChange)unit;
  			if (change.className.equals("DigitalSignature")){
  				SOAPMessage msg = messageFactory.createMessage();
  				msg.getSOAPPart().getEnvelope()
  					 .addNamespaceDeclaration("ion","http://xsd.smev.ru/ppu/ionProcessingService");
  				
  				msg.getMimeHeaders()
  					 .addHeader("SOAPAction",
  											"attachECPRequest");
  				
  				addSecurityTokenHeader(msg, change.author, token);
  								
  				SOAPBodyElement bl = msg.getSOAPBody()
  																.addBodyElement(new QName("http://xsd.smev.ru/ppu/ionProcessingService",
  																													"attachECPRequest",
  																													"ion"));
  				
  				SOAPElement el = bl.addChildElement(new QName("requests"));
  				
  				el = el.addChildElement(new QName("request"));
  				el.addAttribute(new QName("cls"), change.data.get("class").toString());
  				el.addAttribute(new QName("id"), change.data.get("id").toString());
  				el.addAttribute(new QName("operation"), change.data.get("action").toString());
  
  				@SuppressWarnings("unchecked")
					Map<String, String> attrs = (Map<String, String>)change.data.get("attributes");
  								
  				SOAPElement sig = el.addChildElement(new QName("signature"));
  				sig.addAttribute(new QName("type"), attrs.get("attachType"));
  
  				if (change.data.get("class").toString().equals("rqstMain") && attrs.get("attachType").equals("sedECP")){
  					el = sig.addChildElement(new QName("attrs"));
  					for (Map.Entry<String, String> a: attrs.entrySet())
  						if (a.getKey().startsWith("a.")){
  							SOAPElement attrel = el.addChildElement(new QName("attr"));
  							attrel.addAttribute(new QName("name"), a.getKey().replaceFirst("\\Aa\\.", ""));
  							attrel.setTextContent(a.getValue());
  						}
  				} else {
  					el = sig.addChildElement(new QName("data"));
  					el.setTextContent(change.data.get("data").toString());
  				}
  				
  				el = sig.addChildElement(new QName("content"));
  				el.setTextContent(change.data.get("signature").toString());
  				
    			msg.saveChanges();
    			request(getSignaturesAttachEndpoint(), msg);
    			return new HashMap<String, String>();
  			}			
			
			
			
  			URL endpoint = getMainEndpoint();

    		SOAPMessage msg = messageFactory.createMessage();
    
    		String nsprefix = "man";
    		String nsuri = "http://xsd.smev.ru/ion/offline/ManageGenericItemsRequest";

    		msg.getSOAPPart().getEnvelope()
    			.addNamespaceDeclaration(nsprefix, nsuri);    		
    		
    		msg.getMimeHeaders().addHeader("SOAPAction", "ManageGenericItemsRequest");
    			
    		addSecurityTokenHeader(msg, change.author, token);
    
    		SOAPBodyElement bl = msg.getSOAPBody()
    																	.addBodyElement(new QName(nsuri, "ManageGenericItemsRequest", nsprefix));
    			
    		SOAPElement el = bl.addChildElement(new QName(nsuri, "genericObjs", nsprefix));
    		el = el.addChildElement(new QName(nsuri, "genericObj", nsprefix));
  
    		SOAPElement el1 = el.addChildElement(new QName(nsuri, "className", nsprefix));
    		
    		el1.setTextContent(unit.className);
    		
    		
    	/*	
    	 * TODO Реализовать настройкой
    		el1 = el.addChildElement(new QName("disableHandlers"));
    		el1.setTextContent("true");
    		*/
    		
    		el1 = el.addChildElement(new QName(nsuri, "operationType", nsprefix));
    		
    		if (((DataChange)unit).action.equals("create"))
    			el1.setTextContent("CREATE");
    		else
    			el1.setTextContent("UPDATE");
    		
    		el = el.addChildElement(new QName(nsuri, "genericItems", nsprefix));
    		el = el.addChildElement(new QName(nsuri, "genericItem", nsprefix));

    		Map<String, StoredClassMeta> classes = new HashMap<String, StoredClassMeta>();
    		Map<String, String> classWsdl = new HashMap<String, String>();
    		
    		StoredClassMeta cm = requestClass(unit.className, classes, classWsdl);
    		ClassAssembler ca = new ClassAssembler(cm, classes);

    		if (!((DataChange)unit).action.equals("create")){
    			el1 = el.addChildElement(new QName(nsuri, "identifier", nsprefix));
    			el1.setAttribute("name", ca.key);
    			el1.setTextContent(unit.id);
    		}
    		
    		el = el.addChildElement(new QName(nsuri, "attributes", nsprefix));
    		
    		SOAPElement el2;
    		SOAPElement el3;
    		
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    		
    		for (StoredPropertyMeta pm: ca.properties.values())
    			if (unit.data.containsKey(pm.name) && !pm.name.startsWith("offln")){
    				el1 = el.addChildElement(new QName(nsuri, "attr", nsprefix));
    				el1.setAttribute("name", pm.name);
      			if (pm.type != MetaPropertyType.COLLECTION.getValue()) {
      				Object v = unit.data.get(pm.name);
      				if (v != null){
      					if (pm.type == MetaPropertyType.REFERENCE.getValue()){
      						el2 = el1.addChildElement(new QName(nsuri, "genericObj", nsprefix));
      						el3 = el2.addChildElement(new QName(nsuri, "className", nsprefix));
      						el3.setTextContent(pm.ref_class);
      						el3 = el2.addChildElement(new QName(nsuri, "operationType", nsprefix));
      						el3.setTextContent("LINK");
      						el3 = el2.addChildElement(new QName(nsuri, "genericItems", nsprefix));
      						el3 = el3.addChildElement(new QName(nsuri, "genericItem", nsprefix));
      						el3 = el3.addChildElement(new QName(nsuri, "identifier", nsprefix));
      						
      						StoredClassMeta refcm = requestClass(pm.ref_class, classes, classWsdl);
      						el3.setAttribute("name", refcm.key.toArray(new String[1])[0]);
      						el3.setTextContent(v.toString());
      					} else {
      						if (pm.type == MetaPropertyType.DATETIME.getValue())
      							el1.setTextContent(format.format((Date)v));
      						else
      							el1.setTextContent(v.toString());
      					}
      						
      					result.put(pm.name, v.toString());
      				}
      			} else {
      				@SuppressWarnings("unchecked")
							Collection<Map<String,String>> collection = (Collection<Map<String,String>>)unit.data.get(pm.name);
      				
          		StoredClassMeta ccm = requestClass(pm.items_class, classes, classWsdl);
      				
      				el2 = el1.addChildElement(new QName(nsuri, "genericObj", nsprefix));
      				el3 = el2.addChildElement(new QName(nsuri, "className", nsprefix));
      				el3.setTextContent(pm.items_class);
      				/*
      				 * TODO Вынести в настройки
      				el3 = el2.addChildElement("disableHandlers");
      				el3.setTextContent("true");
      				*/
      				
      				el3 = el2.addChildElement(new QName(nsuri, "operationType", nsprefix));
      				el3.setTextContent("LINK");
      				el2 = el2.addChildElement(new QName(nsuri, "genericItems", nsprefix));
      				for (Map<String, String> item: collection){
      					el3 = el2.addChildElement(new QName(nsuri, "genericItem", nsprefix));
      					el3 = el3.addChildElement(new QName(nsuri, "identifier", nsprefix));
      					el3.setAttribute("name", ccm.key.toArray(new String[1])[0]);
      					el3.setTextContent(item.get("id"));
      				}
      			}
    			}
    		
    		/*
    		el = bl.addChildElement(new QName("zipResponse"));
    		el.setTextContent("true");
    		 */
    		
  			msg.saveChanges();
  			request(endpoint, msg);
  		} catch (DOMException | SOAPException | UnsupportedOperationException | IOException e) {
  			throw new IonException(e);
  		}
		}
		return result;
	}

	public void setMainWsdl(String mainWsdl) {
		this.mainWsdl = mainWsdl;
	}

}
