package ion.offline.adapters;

import ion.core.ConditionType;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.logging.ILogger;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredMatrixEntry;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSelectionProvider;
import ion.offline.net.ClassPermission;
import ion.offline.net.DataChange;
import ion.offline.net.DataChangeType;
import ion.offline.net.DataUnit;
import ion.offline.net.UserProfile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SitexSoapRequestor extends BasicSitexSoapRequestor {

/*
	@SuppressWarnings("serial")
	final private static Set<String> jkw					 = new HashSet<String>() {
																									 {
																										 add("abstract");
																										 add("assert");
																										 add("break");
																										 add("case");
																										 add("catch");
																										 add("class");
																										 add("const");
																										 add("continue");
																										 add("default");
																										 add("do");
																										 add("double");
																										 add("else");
																										 add("enum");
																										 add("extends");
																										 add("final");
																										 add("finally");
																										 add("float");
																										 add("for");
																										 add("goto");
																										 add("if");
																										 add("implements");
																										 add("import");
																										 add("instanceof");
																										 add("int");
																										 add("interface");
																										 add("long");
																										 add("native");
																										 add("new");
																										 add("package");
																										 add("private");
																										 add("protected");
																										 add("public");
																										 add("return");
																										 add("short");
																										 add("static");
																										 add("strictfp");
																										 add("super");
																										 add("switch");
																										 add("synchronized");
																										 add("this");
																										 add("throw");
																										 add("throws");
																										 add("transient");
																										 add("try");
																										 add("void");
																										 add("volatile");
																										 add("while");
																										 add("false");
																										 add("null");
																										 add("true");
																									 }
																								 };
*/

	public SitexSoapRequestor() throws IonException {
		super();
		_classWsdl = new TreeMap<String, String>();
	}
/*
	private String escapeJavaKeyword(String name) {
		if (name != null && jkw.contains(name))
			return "jkw_" + name;
		return name;
	}
*/

	private String capitalize(String input) {
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	private int parsePrimitive(String type, Integer recommended) throws IonException {
		if (type.equals("integer") || type.equals("int") || type.equals("long")
				|| type.equals("negativeInteger") || type.equals("nonNegativeInteger")
				|| type.equals("nonPositiveInteger") || type.equals("positiveInteger")
				|| type.equals("short") || type.equals("unsignedLong")
				|| type.equals("unsignedInt") || type.equals("unsignedShort")
				|| type.equals("unsignedByte"))
			return MetaPropertyType.INT.getValue();

		if (type.equals("decimal") || type.equals("double") || type.equals("float"))
			return MetaPropertyType.DECIMAL.getValue();

		if (type.equals("boolean"))
			return MetaPropertyType.BOOLEAN.getValue();

		if (type.equals("date") || type.equals("dateTime"))
			return MetaPropertyType.DATETIME.getValue();

		if (type.equals("base64Binary"))
			return MetaPropertyType.FILE.getValue();
		
		if (type.equals("string") && (recommended != null) && (recommended == MetaPropertyType.TEXT.getValue()))
			return recommended;
		
/*
		TODO возвращать скалярный тип

		if (recommended != null)
			return recommended;
*/		
		return MetaPropertyType.STRING.getValue();
	}

	private Node findSimpleType(Object[] r, String name, Node attr)
																											throws XPathExpressionException, DOMException, 
																											ParserConfigurationException, SAXException, IOException {
		Document doc = (Document) r[0];
		XPath parser = (XPath) r[1];
		Object[] locator = r;
		XPathExpression expr = null;
		Node result = null;
		if (!name.isEmpty()){
			expr = parser.compile("//*[local-name()='simpleType'][@name='"
				+ name + "']/*[local-name()='restriction']");
			result = (Node) expr.evaluate(doc, XPathConstants.NODE);
		} else {
			expr = parser.compile("*[local-name()='simpleType']/*[local-name()='restriction']");
			result = (Node)expr.evaluate(attr, XPathConstants.NODE);
		}
		
		if (result != null && result.getAttributes().getNamedItem("base") != null) {
			String test = result.getAttributes().getNamedItem("base")
								.getNodeValue();

			if (test.contains(":")) {
				String[] parts = test.split(":");
				if (parts[0].equals("xsd"))
					return result;
				else {
					locator = changeLocator(r, parts[0], getRelatedSchemas(doc, parser));
					test = parts[1];
				}
			}
			return findSimpleType(locator, test, null);
		}
		return null;
	}

	private NodeList findComplexTypeElements(Object[] r, String name)
																																		throws XPathExpressionException {
		Document doc = (Document) r[0];
		XPath parser = (XPath) r[1];
		XPathExpression expr = parser.compile("//*[local-name()='complexType'][@name='"
				+ name + "']/*[local-name()='sequence']/*");
		return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
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

	private Object[] changeLocator(Object[] locator, String prefix,
																 Map<String, String> relatedSchemas)
																																		throws ParserConfigurationException,
																																		SAXException,
																																		IOException {
		if (relatedSchemas.containsKey(prefix))
			if (relatedSchemas.containsKey(prefix) &&
					relatedSchemas.get(prefix) != null
					&& !relatedSchemas.get(prefix).isEmpty())
				return getSchemaReader(relatedSchemas.get(prefix),
															 new HashMap<String, String>());
		return locator;
	}
	/*
	private StoredClassMeta loadClass(Object[] locator, String t,
	                                  Map<String, StoredClassMeta> classes,
	                                  Map<String, Map<String, String>> classAttrTabs,
	                                  Map<String, Map<String, String>> classAttrGroups,
	                                  Map<String, String> classWsdl, 
	                                  Map<String, Integer> dictPermissions,
	                                  Map<String, Set<String>> dictionaries) throws XPathExpressionException, DOMException, ParserConfigurationException, SAXException, IOException, IonException, SOAPException {
		return loadClass(locator, t, classes, classAttrTabs, classAttrGroups, classWsdl, dictPermissions, null, dictionaries);
	}
	*/
	private StoredClassMeta loadClass(Object[] locator, String t, 
	                                  Map<String, StoredClassMeta> classes,
	                                  Map<String, Map<String, String>> classAttrTabs,
	                                  Map<String, Map<String, String>> classAttrGroups,
	                                  Map<String, String> classWsdl, 
	                                  Map<String, Integer> dictPermissions, 
	                                  StoredClassMeta br, Map<String, Set<String>> dictionaries, 
	                                  Map<String, String> classTypes, List<UserProfile> profiles, 
	                                  Map<String, Map<String,String>> collectionLinkClasses) 
	                          throws XPathExpressionException, DOMException, ParserConfigurationException, 
	                          			 SAXException, IOException, IonException, SOAPException, 
	                          			 UnsupportedOperationException, URISyntaxException, TransformerException {
		if (classes.containsKey(t)){
			logger.Info("class " + t + " already loaded");
			return classes.get(t);
		}
		
		StoredClassMeta refc = getClassFromCache(t, classes, classWsdl, dictPermissions, classAttrGroups);
		
		if (refc == null){
			Map<String, String> r_attrCaptions = new HashMap<String, String>();
			Map<String, Integer> r_attrOrder = new HashMap<String, Integer>();
			Map<String, Integer> r_attrTypes = new HashMap<String, Integer>();
			Map<String, Short> r_attrSize = new HashMap<String, Short>();
			Map<String, String> r_attrRefClass = new HashMap<String, String>();
			
			Object[] params = getClassMeta(t, classAttrTabs, classAttrGroups,
																		 r_attrOrder, r_attrSize,
																		 r_attrTypes, r_attrCaptions, r_attrRefClass, 
																		 dictionaries, collectionLinkClasses);

			refc = loadClassFromSchema(locator, t, (String) params[1],
			                           (String) params[2], classes,
			                           classAttrTabs, classAttrGroups, r_attrCaptions, r_attrTypes,
																 r_attrOrder, r_attrSize, r_attrRefClass,
																 classWsdl, dictPermissions, br, dictionaries, classTypes, profiles, collectionLinkClasses);			
			if (refc != null)
				classes.put(t, refc);
		}
		return refc;
	}
	
	private StoredClassMeta loadClassFromSchema(Object[] r,
																							String cn,
																							String caption,
																							String semantics,
																							Map<String, StoredClassMeta> classes,
																							Map<String, Map<String, String>> classAttrTabs,
																							Map<String, Map<String, String>> classAttrGroups,
																							Map<String, String> attrCaptions,
																							Map<String, Integer> attrTypes,
																							Map<String, Integer> attrOrder,
																							Map<String, Short> attrSize,
																							Map<String, String> attrRefClasses,
																							Map<String, String> classWsdl,
																							Map<String, Integer> dictPermissions,
																							Map<String, Set<String>> dictionaries,
																							Map<String, String> classTypes,
																							List<UserProfile> profiles,
																							Map<String, Map<String, String>> collectionLinkClasses)
																															 throws ParserConfigurationException,
																															 SAXException,
																															 IOException,
																															 XPathExpressionException,
																															 DOMException,
																															 IonException,
																															 SOAPException, UnsupportedOperationException, 
																															 URISyntaxException, TransformerException {
		return loadClassFromSchema(r, cn, caption, semantics, classes, classAttrTabs, classAttrGroups, 
		                           attrCaptions, attrTypes, attrOrder, attrSize, attrRefClasses, classWsdl, 
		                           dictPermissions, null, dictionaries, classTypes, profiles, collectionLinkClasses);
	}
	
	private Map<String, String> getRelatedSchemas(Document doc, XPath parser) throws XPathExpressionException, DOMException {
		NamedNodeMap ns = doc.getFirstChild().getAttributes();
		Map<String, String> relatedSchemas = new HashMap<String, String>();
		
		XPathExpression expr;
		
		for (int i = 0; i < ns.getLength(); i++)
			if (ns.item(i).getPrefix() != null) {
				if (ns.item(i).getPrefix().equals("xmlns")) {
					expr = parser.compile("//*[local-name()='import'][@namespace='"
							+ ns.item(i).getNodeValue() + "']/@schemaLocation");
					relatedSchemas.put(ns.item(i).getLocalName(),
														 (String) expr.evaluate(doc, XPathConstants.STRING));
				}
			}
		return relatedSchemas;
	}
	
	
	@SuppressWarnings("unchecked")
	private void loadRqstMainSelectionLists(Collection<StoredMatrixEntry> rqstTypeSL, 
	                                        Collection<StoredMatrixEntry> pettOrgSL, 
	                                        Collection<UserProfile> profiles) 
	                                        		throws UnsupportedOperationException, XPathExpressionException, 
	                                        						SOAPException, IOException, ParserConfigurationException, 
	                                        						SAXException, DOMException, URISyntaxException, 
	                                        						TransformerException, IonException {
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
	  		
	  		String orgWsdl = getClassWsdl("egOrganization");
	  		if (orgWsdl != null){
  	  		Map<String, StoredClassMeta> classes = new HashMap<String, StoredClassMeta>();
  	  		StoredClassMeta orgClass = getStoredClassMeta("egOrganization", classes, new HashMap<String, Set<String>>(), new HashMap<String, Map<String,String>>());
  	  		ClassAssembler orgClassA = new ClassAssembler(orgClass, classes);
  	  		
  	  		for (UserProfile p: profiles) {
  	  			if (p.properties.containsKey("organisations")){
  	  				Set<String> porgs = (Set<String>)p.properties.get("organisations");
  	  				for (String org: porgs){
  	  					if (!orgParents.containsKey(org)) {
  	  						Map<String, Object> filter = new HashMap<String, Object>();
  	  						filter.put("guid", org);
  	  						@SuppressWarnings("serial")
									List<DataUnit> parents = fetchData(orgClassA, filter, classes, 
  	  						                                   new HashMap<String, Set<String>>(){{
  	  						                                  	 put("egOrganization",new HashSet<String>(){{
  	  						                                  		 add("parent");
  	  						                                  	 }});
  	  						                                   }}, 0);
  	  						Set<String> prnts = new LinkedHashSet<String>();
  	  						prnts.add(org);
  	  						for (DataUnit orgO: parents)
  	  							prnts.add(orgO.id);
  	  						orgParents.put(org, prnts);
  	  					}
  	  				}
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
	}

	@SuppressWarnings("serial")
	private StoredClassMeta loadClassFromSchema(Object[] r,
																							String cn,
																							String caption,
																							String semantics,
																							Map<String, StoredClassMeta> classes,
																							Map<String, Map<String, String>> classAttrTabs,
																							Map<String, Map<String, String>> classAttrGroups,
																							Map<String, String> attrCaptions,
																							Map<String, Integer> attrTypes,
																							Map<String, Integer> attrOrder,
																							Map<String, Short> attrSize,
																							Map<String, String> attrRefClasses,
																							Map<String, String> classWsdl,
																							Map<String, Integer> dictPermissions,
																							StoredClassMeta backref,
																							Map<String, Set<String>> dictionaries,
																							Map<String, String> classTypes,
																							List<UserProfile> profiles,
																							Map<String, Map<String, String>> collectionLinkClasses)
																															 throws ParserConfigurationException,
																															 SAXException,
																															 IOException,
																															 XPathExpressionException,
																															 DOMException,
																															 IonException,
																															 SOAPException, UnsupportedOperationException, 
																															 URISyntaxException, TransformerException {
		
		logger.Info("schema loading class " + cn);

		Document doc = (Document) r[0];
		XPath parser = (XPath) r[1];
		XPathExpression expr;
		XPathExpression captExpr;

		Map<String, String> relatedSchemas = getRelatedSchemas(doc, parser);

		expr = parser.compile("//*[local-name()='complexType'][@name='" + cn	+ "'][1]");
		Node classNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
		if (classNode == null){
			expr = parser.compile("//*[local-name()='simpleType'][@name='" + cn	+ "'][1]");
			classNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
		}
		
		captExpr = parser.compile("*[local-name()='annotation']/*[local-name='documentation']/text()");
		
		if (classNode != null) {
			if (caption == null || caption.isEmpty()){
				caption = (String)captExpr.evaluate(classNode, XPathConstants.STRING);
				caption = caption.replaceAll("\\s+"+cn, "");
			}
			
			StoredClassMeta cm = new StoredClassMeta(false,
																							 null,
																							 /*escapeJavaKeyword(*/cn/*)*/,
																							 caption,
																							 semantics,
																							 null,
																							 new LinkedList<StoredPropertyMeta>());
			_classes.put(cn, cm);

			expr = parser.compile("*[local-name()='sequence']/*");

			NodeList attrs = (NodeList) expr.evaluate(classNode,
																								XPathConstants.NODESET);
			String key = "";
			boolean has_guid = false, has_ouid = false, has_id = false, has_code = false, has_oid = false, has_kladrCode = false;

			String classType = classTypes.containsKey(cn)?classTypes.get(cn):"";
			
			String ancestor = classType.equals(CT_FEDERAL) ? SitexServicePortalAdapter.CN_FEDERAL
			                                     											: (classType.equals(CT_REGIONAL) ? SitexServicePortalAdapter.CN_REGIONAL
			                                     											: (classType.equals(CT_PETITION) ? SitexServicePortalAdapter.CN_PETITION: ""));			
			
			Map<String, String> visibleAttrs = null;
			if (classAttrTabs.containsKey(cn))
				visibleAttrs = classAttrTabs.get(cn);
			else
				visibleAttrs = new HashMap<String, String>();
			
			
			Map<String, Object[]> attrLocators = new HashMap<String, Object[]>();
			
			Collection<StoredMatrixEntry> rqstTypeSL = null;
			Collection<StoredMatrixEntry> pettOrgSL = null;
			
			if (attrs != null)
				for (int i = 0; i < attrs.getLength(); i++) {
					Node attr = attrs.item(i);

					String attrName = /*escapeJavaKeyword(*/attr.getAttributes().getNamedItem("name")
																.getNodeValue()/*)*/;
					
					if (!attrCaptions.isEmpty() && !attrCaptions.containsKey(attrName))
						continue;

					if (!attrName.matches("[a-zA-Z][a-zA-Z0-9_\\$]*") ||
							attrName.equals("systemClass") ||
							((attrName.equals("overdue") || attrName.equals("timeStamp") || attrName.equals("createDate")) && !ancestor.isEmpty()) || 
							(attrName.equals("history") && ancestor.equals(SitexServicePortalAdapter.CN_PETITION)))
						continue;

					String capt = "";
					if (attrCaptions.containsKey(attrName))
						capt = attrCaptions.get(attrName);
					else {
						capt = (String)captExpr.evaluate(attr, XPathConstants.STRING);
						if (capt != null && !capt.isEmpty())
							capt = capt.trim();
						else
							capt = attrName;
					}

					int type = -1;
					
					boolean nullable = true;
					boolean autoassigned = false;
					
					String refClass = null;
					String itemsClass = null;
					
					Boolean eagerLoading = false;
					StoredSelectionProvider selection = null;

					if (attrName.equals("guid")) {
						has_guid = true;
						type = MetaPropertyType.GUID.getValue();
						nullable = false;
						autoassigned = true;
					} else {
						if (attrName.equals("ouid"))
							has_ouid = true;
						else if (attrName.equals("oid"))
							has_oid = true;
						else if (attrName.equals("id"))
							has_id = true;
						else if (attrName.equals("kladrCode"))
							has_kladrCode = true;
						/*else if (attrName.equals("dictionaryCode"))
							has_dictCode = true;*/
						else if (attrName.equals("code"))
							has_code = true;
						else if (!ancestor.isEmpty())
							nullable = attr.getAttributes().getNamedItem("nillable") != null
									&& attr.getAttributes().getNamedItem("nillable").getNodeValue().equals("true");
						
						if (!visibleAttrs.containsKey(attrName) || visibleAttrs.get(attrName).equals("Общая информация"))
							nullable = true;
						
						String t  = "";
						if (attr.getAttributes().getNamedItem("type") != null){
							t = attr.getAttributes().getNamedItem("type").getNodeValue();
						}
						
						Object[] locator = r;

						if (t.contains(":")) {
							String[] parts = t.split(":");
							t = parts[1];
							if (parts[0].equals("xsd")) {
								type = parsePrimitive(t, attrTypes.containsKey(attrName)?attrTypes.get(attrName):null);
							} else {
								locator = changeLocator(locator, parts[0], relatedSchemas);
								attrLocators.put(attrName, locator);
							}
						}
						
						if (t.equals("SXClass") || t.equals("SXRole") || t.equals("SXGroup") || t.equals("sedECP"))
							continue;

						if (type == -1) {							
							Node simple = findSimpleType(locator, t, attr);

							if (simple != null) {
								if (simple.getAttributes().getNamedItem("base") != null) {
									t = simple.getAttributes().getNamedItem("base")
														.getNodeValue();

									if (t.contains(":")) {
										String[] parts = t.split(":");
										if (parts[0].equals("xsd"))
											type = parsePrimitive(parts[1], attrTypes.containsKey(attrName)?attrTypes.get(attrName):null);
									}
								}

								if (type == -1)
									throw new IonException("Не удалось определить тип атрибута "
											+ cn + "." + attrName + ".");
								else {
									List<StoredKeyValue> sellist = new LinkedList<StoredKeyValue>();
									Node enm = simple.getFirstChild();
									while (enm != null) {
										if (enm.getNodeName().equals("enumeration")) {
											String text = enm.getTextContent().trim();
											String value = text;
											if (enm.getAttributes().getNamedItem("value") != null)
												value = enm.getAttributes().getNamedItem("value")
																	 .getNodeValue();
											sellist.add(new StoredKeyValue(value, text));
										}
										enm = enm.getNextSibling();
									}
									selection = new StoredSelectionProvider(sellist);
								}
							} else {
								NodeList complex = findComplexTypeElements(locator, t);
								if (complex != null && complex.getLength() > 0) {
									boolean collection = false;
									if (complex.item(0).getAttributes().getNamedItem("maxOccurs") != null) {
										if (complex.item(0).getAttributes()
															 .getNamedItem("maxOccurs").getNodeValue()
															 .equals("unbounded")) {
											collection = true;
											t = complex.item(0).getAttributes().getNamedItem("type")
																 .getTextContent();
											
											if (t.contains(":")) {
												String[] parts = t.split(":");
												t = parts[1];
												locator = changeLocator(locator, parts[0],
																								relatedSchemas);
												attrLocators.put(attrName, locator);
											}
										}
									}
									
									if (t != null && (t.equals("SXClass") 
											|| t.equals("SXRole") 
											|| t.equals("SXGroup") 
											|| t.equals("sedECP")))
										continue;
									
									if (collection){
										type = MetaPropertyType.COLLECTION.getValue();
										itemsClass = t;
									} else {
										type = MetaPropertyType.REFERENCE.getValue();
										refClass = t;
									}
								}
							}
						}
					}

					if (!attrName.equals("guid") && attrTypes.containsKey(attrName) && type == -1)
						type = attrTypes.get(attrName);

					if (type != -1) {
						int orderNum = i + 1;
						if (attrName.equals("guid") || attrName.equals("ouid")
								|| attrName.equals("oid") || attrName.equals("code") 
								|| attrName.equals("id") || attrName.equals("kladrCode"))
							orderNum = 0;
						else if (attrOrder.containsKey(attrName))
							orderNum = attrOrder.get(attrName);
						
						if (ancestor.isEmpty() || !(attrName.equals("guid") || attrName.equals("ouid"))){
							if (cm.name.equals("rqstMain")){
								if (attrName.equals("rqstTyp")){
									if (rqstTypeSL == null) {
										rqstTypeSL = new LinkedList<StoredMatrixEntry>();
										pettOrgSL = new LinkedList<StoredMatrixEntry>();
										loadRqstMainSelectionLists(rqstTypeSL, pettOrgSL, profiles);
									}
									selection = new StoredSelectionProvider(rqstTypeSL);									
								} else if (attrName.equals("pettOrg")) {
									if (pettOrgSL == null) {
										rqstTypeSL = new LinkedList<StoredMatrixEntry>();
										pettOrgSL = new LinkedList<StoredMatrixEntry>();
										loadRqstMainSelectionLists(rqstTypeSL, pettOrgSL, profiles);
									}
									selection = new StoredSelectionProvider(pettOrgSL);
								}
							}						
							
							cm.properties.add(new StoredPropertyMeta(orderNum + 6,
																										 attrName,
																										 capt,
																										 type,
																										 (attrSize.containsKey(attrName) && type == MetaPropertyType.STRING.getValue()) ? attrSize.get(attrName)
																																																																	 : null,
																										 null,
																										 nullable,
																										 (attrName.equals("guid")
																												 || attrName.equals("oid") || attrName.equals("ouid")),
																										 false,
																										 false,
																										 autoassigned,
																										 "",
																										 null,
																										 refClass,
																										 itemsClass,
																										 null,
																										 null,
																										 null,
																										 null,
																										 null,
																										 selection,
																										 false, 
																										 eagerLoading,null,null));
						}
					}
			}
			
			ancestor = has_guid?ancestor:"";

			if (ancestor.isEmpty())
				key = has_guid ? "guid"
											: ((has_ouid && !cm.name.equals("smevPetitionStatus"))? "ouid" : 
												((has_id && !cm.name.equals("smevPetitionStatus")) ? "id" : 
													(has_kladrCode ? "kladrCode" : (has_code ? "code" : (has_oid ? "oid" : "")))));

			if (!key.isEmpty()){
				cm.key = new LinkedList<String>();
				cm.key.add(key);
			}
			cm.ancestor = ancestor.isEmpty() ? null : ancestor;
			if ((cm.key == null || cm.key.isEmpty()) && (cm.ancestor == null) && cm.properties.isEmpty()){
				cm.key = new LinkedList<String>(){{add("offlnSvId");}};
				
				cm.properties.add(new StoredPropertyMeta(0, "offlnSvId", "Идентификатор", MetaPropertyType.INT.getValue(),
				                                   			(short)20, null, false, true, true, true, true, "", null, null, null, null, null, null, 
				                                  			null,null,false, false,null,null));
				
				int svtype = MetaPropertyType.STRING.getValue();
				
				if (classNode.getAttributes().getNamedItem("base") != null) {
					String svt = classNode.getAttributes().getNamedItem("base")
										.getNodeValue();

					if (svt.contains(":")) {
						String[] parts = svt.split(":");
						if (parts[0].equals("xsd"))
							svtype = parsePrimitive(parts[1], null);
					}
				}
				                                         
				cm.properties.add(new StoredPropertyMeta(2, "offlnSvVal", "Значение", svtype,
				                                   			(svtype == MetaPropertyType.STRING.getValue())?(short)500:null, null, true, false, false, false, false, "", null, null, null, null, null, 
				                                   			null, null,null,false, false,null,null));

				Map<String, String> hm = new HashMap<String, String>();
				hm.put("offlnSvVal", "Основная информация");
				classAttrTabs.put(cm.name, hm);
				
				
				if (backref != null){
					cm.properties.add(new StoredPropertyMeta(1, "offlnSvBref", "Контейнер", MetaPropertyType.REFERENCE.getValue(),
        			null, null, true,true, true, false, false, "", null, backref.name, null, null, null, null, 
        			null,null,false, false,null,null));
				}
			} else {
				String cwsdl = getClassWsdl(cm.name);
				if (cwsdl != null)
					classWsdl.put(cm.name, cwsdl);
			}
			
			if (cm.ancestor == null || cm.ancestor.isEmpty()){
				int dperm = 0;
				if (dictPermissions.containsKey(cm.name))
					dperm = dictPermissions.get(cm.name);
				dperm = dperm | ClassPermission.READ.getValue() | ClassPermission.CREATE.getValue();
				dictPermissions.put(cm.name, dperm);
			}
			
			if ((cm.key == null || cm.key.isEmpty()) && (cm.ancestor == null || cm.ancestor.isEmpty()))
				cm.is_struct = true;
			
			for (StoredPropertyMeta pm: cm.properties){
				if (pm.type == MetaPropertyType.COLLECTION.getValue() || pm.type == MetaPropertyType.REFERENCE.getValue()){
					Object[] locator = r;
					String t = (pm.type == MetaPropertyType.COLLECTION.getValue())?pm.items_class:pm.ref_class;

					if (attrLocators.containsKey(pm.name)) {
						locator = attrLocators.get(pm.name);
					}
					
					StoredClassMeta refc = null;
					
					if (t != null) {
						logger.Info("loading class " + t + " for refattribute " + cn + "." + pm.name);
						refc = loadClass(locator, t, classes, classAttrTabs, classAttrGroups, classWsdl, dictPermissions, cm, dictionaries, classTypes, profiles, collectionLinkClasses);
					}
					
					if (refc == null) {
						if (pm.type == MetaPropertyType.REFERENCE.getValue()){
							if (attrRefClasses.containsKey(pm.name)){
								pm.ref_class = attrRefClasses.get(pm.name);
								logger.Info("x:loading class " + pm.ref_class + " for refattribute " + cn + "." + pm.name);
								refc = requestClass(pm.ref_class, classes, classAttrTabs, classAttrGroups, classWsdl, dictPermissions, dictionaries, classTypes, profiles, collectionLinkClasses);
							}
						}

						if (pm.type == MetaPropertyType.COLLECTION.getValue()){
							if (attrRefClasses.containsKey(pm.name)){
								pm.items_class = attrRefClasses.get(pm.name);
								logger.Info("x:loading class " + pm.items_class + " for colattribute " + cn + "." + pm.name);
								refc = requestClass(pm.items_class, classes, classAttrTabs, classAttrGroups, classWsdl, dictPermissions, dictionaries, classTypes, profiles, collectionLinkClasses);
							}
						}													
					}					
					
					if (refc == null)
						logger.Warning("Не удалось найти класс "
								+ ((pm.type == MetaPropertyType.COLLECTION.getValue()) ? "коллекции " : "ссылки ") + cn + "."
								+ pm.name + ".");
					else {
  					if (pm.type == MetaPropertyType.COLLECTION.getValue()) {
  						pm.items_class = refc.name;
  						if (refc.key != null && refc.key.equals("offlnSvId")) {
  							pm.back_ref = "offlnSvBref";
  						} else {
  							for (StoredPropertyMeta spm:refc.properties) {
  								if (spm.type == MetaPropertyType.REFERENCE.getValue() && spm.ref_class.equals(cn)) {
  									pm.back_ref = spm.name;
  									break;
  								}													
  							}
  						}
  					} else {
  						if (refc.is_struct)
  							pm.type = MetaPropertyType.STRUCT.getValue();
  						pm.ref_class = refc.name;
  					}
  						
  					if(eagerClasses.contains(refc.name))
  						pm.eager_loading = true;
					}
				}
			}
			
			if (!ancestor.isEmpty()){
				checkParentClass(cm, classes, classAttrTabs);
				if (cm.ancestor.equals(SitexServicePortalAdapter.CN_PETITION)){
		  		Map<String, String> decisions = fetchResolutionDesc(cm.name);
		  		cm.properties.add(new StoredPropertyMeta(5,
		  		                                           "offlnResolutionDecision",
		  																								"Решение",
		  																								MetaPropertyType.INT.getValue(),
		  																								null,null,
		  																								true,false,false,false,false,"",
		  																								null,null,null,null,null,null,
		  																								null,null,new StoredSelectionProvider(decisions),false,false,null,null));
		  		/*
		  		if (classAttrTabs.containsKey(cm.name)){
		  			Map<String, String> ag = classAttrTabs.get(cm.name); 
		  			ag.remove("resolution");
		  			ag.put("offlnResolutionDecision", "Данные ответа");
		  		}
		  		*/
				}
			}
			return cm;
		}
		logger.Warning("Class not found for name " + cn);
		_classes.put(cn, null);
		return null;
	}

	private Integer parseTypeName(String tn) {
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
		return null;
	}

	private Object[] getClassMeta(String cn,
	                              Map<String, Map<String, String>> classAttrTabs,
	                              Map<String, Map<String, String>> classAttrGroups,
																Map<String, Integer> attrOrder,
																Map<String, Short> attrSize,
																Map<String, Integer> attrTypes,
																Map<String, String> attrCaptions,
																Map<String, String> refClasses,
																Map<String, Set<String>> dictionaries,
																Map<String, Map<String,String>> collectionLinkClasses) {
		boolean has_guid = false;
		String caption = cn;
		String semantics = "";

		try {
			SOAPMessage msg = messageFactory.createMessage();
			msg.getSOAPPart().getEnvelope()
				 .addNamespaceDeclaration("user", "http://sys.smev.ru/xsd/user");
			msg.getMimeHeaders()
				 .addHeader("SOAPAction",
										"http://sys.smev.ru/xsd/user/User/getSXClassRequest");
			
			addSecurityTokenHeader(msg, sysLogin, sysToken);
			
			SOAPBodyElement bl = msg.getSOAPBody()
															.addBodyElement(new QName("http://sys.smev.ru/xsd/user",
																												"getSXClassRequest",
																												"user"));
			SOAPElement el = bl.addChildElement(new QName("http://sys.smev.ru/xsd/user",
																										"CondOfSXClass",
																										"user"));
			SOAPElement cl = el.addChildElement(new QName("http://sys.smev.ru/xsd/user",
																										"SXClass",
																										"user"));
			el = cl.addChildElement(new QName("http://sys.smev.ru/xsd/user",
																				"name",
																				"user"));
			el.setTextContent(cn);
			msg.saveChanges();
			msg = request(getProfilesEndpoint(), msg);

			XPath parser = xpathFactory.newXPath();
			
			XPathExpression expr = parser.compile("//*[local-name() = 'SXClass'][*[local-name() = 'name']/text() = '" + cn + "']/*[local-name() = 'attrType']/*[local-name() = 'SXClasss_attrType']");
			NodeList attrs = (NodeList) expr.evaluate(msg.getSOAPBody(),
																								XPathConstants.NODESET);
			XPathExpression caption_expr = parser.compile("//*[local-name() = 'SXClass'][*[local-name() = 'name']/text() = '" + cn + "']/*[local-name() = 'description'][1]/text()");
			caption = (String) caption_expr.evaluate(msg.getSOAPBody(),
																							 XPathConstants.STRING);

			XPathExpression name_expr = parser.compile("*[local-name() = 'NAME']/text()");
			XPathExpression group_expr = parser.compile("*[local-name() = 'GroupType']/text()");

			XPathExpression[] attr_groups_expr = new XPathExpression[3];
			
			attr_groups_expr[0] = parser.compile("*[local-name() = 'attr_group3']/text()");
			attr_groups_expr[1] = parser.compile("*[local-name() = 'attr_group2']/text()");
			attr_groups_expr[2] = parser.compile("*[local-name() = 'attr_group1']/text()");
			
			
			XPathExpression type_expr = parser.compile("*[local-name() = 'type']/*[local-name() = 'logicName'][1]/text()");
			XPathExpression order_expr = parser.compile("*[local-name() = 'Num']/text()");
			XPathExpression size_expr = parser.compile("*[local-name() = 'size']/text()");
			XPathExpression capt_expr = parser.compile("*[local-name() = 'Title']/text()");
			XPathExpression semant_expr = parser.compile("*[local-name() = 'isTitle']/text()");
			XPathExpression show_expr = parser.compile("*[local-name() = 'isShow']/text()");
			XPathExpression rc_expr = parser.compile("*[local-name() = 'refClass']/text()");
			XPathExpression linkClass_expr = parser.compile("*[local-name() = 'linkClass']/text()");
			XPathExpression idattr_expr = parser.compile("*[local-name() = 'idAttr']/text()");

			List<String> semantic_attrs = new LinkedList<String>();
			
			Map<String, String> attrTabs = null;
			
			String ecn = /*escapeJavaKeyword(*/cn/*)*/;
			
			if (classAttrTabs.containsKey(ecn)){
				attrTabs = classAttrTabs.get(ecn);
			} else {
				attrTabs = new HashMap<String, String>();
				classAttrTabs.put(ecn, attrTabs);
			}
			
			Map<String, String> attrGroups = null;
			if (classAttrGroups.containsKey(ecn)){
				attrGroups = classAttrGroups.get(ecn);
			} else {
				attrGroups = new HashMap<String, String>();
				classAttrGroups.put(ecn, attrGroups);
			}
			
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				String name = /*escapeJavaKeyword(*/(String) name_expr.evaluate(attr,
																																		XPathConstants.STRING)/*)*/;
				String tab = (String) group_expr.evaluate(attr, XPathConstants.STRING);
				String group_path = "";
				
				for (int ii = 0; ii < 3; ii++){
					String group_attr = (String) attr_groups_expr[ii].evaluate(attr, XPathConstants.STRING);
					if(group_attr != null && !group_attr.isEmpty() && !group_attr.equals(tab)){
						group_path = group_path + (group_path.isEmpty()?"":"|") + group_attr;
					}
				}
				
				Integer orderNum = i * 10;
				Short size = null;
				try {
					orderNum = Integer.parseInt((String) order_expr.evaluate(attr,
																																				 XPathConstants.STRING));
				} catch (NumberFormatException e) {
					
				}
				try {
					size = Short.parseShort((String) size_expr.evaluate(attr,
																																	XPathConstants.STRING));
				} catch (NumberFormatException e) {
					
				}
				
				String typename = (String) type_expr.evaluate(attr,
																											XPathConstants.STRING);
				String attrCaption = (String) capt_expr.evaluate(attr,
																												 XPathConstants.STRING);
				Integer t = parseTypeName(typename.toLowerCase());
								
				String is_title = (String) semant_expr.evaluate(attr,
																												XPathConstants.STRING);
				if (is_title.equals("true"))
					semantic_attrs.add(name);

				if (name != null) {
					if (name.equals("guid"))
						has_guid = true;
					
					String is_shown = (String) show_expr.evaluate(attr,
																													XPathConstants.STRING);
					if (tab != null && is_shown.equals("true"))
						attrTabs.put(name, tab);
					
					if(!group_path.isEmpty() && is_shown.equals("true"))
						attrGroups.put(name, group_path);
					
					attrOrder.put(name, (orderNum != null) ? orderNum + 1 : 1);
					if (size != null && size != 255)
						attrSize.put(name, size);
					if (t != null){
						attrTypes.put(name, t);
						if (MetaPropertyType.REFERENCE.getValue() == t 
								|| MetaPropertyType.COLLECTION.getValue() == t){
							String rc = (String)rc_expr.evaluate(attr,
																										XPathConstants.STRING);
							if (rc != null) {
  							if (MetaPropertyType.REFERENCE.getValue() == t){
  								if ((typename.equals("ObjectReference") || dictionaryExcludeList.contains(rc)) && !dictionaryIncludeList.contains(rc)){
  									if (!_eagerLoads.containsKey(cn))
  										_eagerLoads.put(cn,new HashSet<String>());
  									_eagerLoads.get(cn).add(name);
  								}
  								if((typename.equals("ObjectsListReference") || dictionaryIncludeList.contains(rc)) && !dictionaryExcludeList.contains(rc)){
  									if(!dictionaries.containsKey(cn))
  										dictionaries.put(cn,new HashSet<String>());
  									dictionaries.get(cn).add(name);
  								}
  							}
  							
  							if(MetaPropertyType.COLLECTION.getValue() == t){
  								if(typename.equals("ObjectsList")){
  									String linkClass = (String)linkClass_expr.evaluate(attr,XPathConstants.STRING);
  									if(linkClass == null){
  										String idattr = (String)idattr_expr.evaluate(attr,XPathConstants.STRING);
  										linkClass = "SXLink:"+idattr;
  									}
  									if(!collectionLinkClasses.containsKey(cn))
  										collectionLinkClasses.put(cn,new HashMap<String,String>());
  									collectionLinkClasses.get(cn).put(name,linkClass);
  								}
  							}
  							
  							refClasses.put(name, rc);
							}
						}
					}
					if (attrCaption != null)
						attrCaptions.put(name, attrCaption);
				}
			}

			final Map<String, Integer> ao = attrOrder;

			Collections.sort(semantic_attrs, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return ao.get(o1) - ao.get(o2);
				}

			});

			semantics = StringUtils.join(semantic_attrs, "| |");
		} catch (Exception e) {
			logger.Error("", e);
		}
		return new Object[] { has_guid, caption, semantics };
	}
	
	private StoredClassMeta getClassFromCache(String cn, Map<String, StoredClassMeta> classes, 
	                                          Map<String, String> classWsdl, Map<String, Integer> dictPermissions,
	                                          Map<String, Map<String, String>> attrGroups) 
	                                          			throws UnsupportedOperationException, SOAPException, 
	                                          							IOException, XPathExpressionException {
		if (classes.containsKey(cn)){
			logger.Info("class "+cn+" already loaded");
			return classes.get(cn);
		}
		if (_classes.containsKey(cn)) {
			StoredClassMeta result = _classes.get(cn);
			if (result != null){
				logger.Info("class " + cn + " found in cache.");
				if (result.ancestor == null || result.ancestor.isEmpty())
					dictPermissions.put(result.name, ClassPermission.CREATE.getValue() | 
					                    ClassPermission.READ.getValue() | ClassPermission.UPDATE.getValue());
				else {
	  			if (!classes.containsKey(SitexServicePortalAdapter.CN_ROOT) && _classes.containsKey(SitexServicePortalAdapter.CN_ROOT))
	  				classes.put(SitexServicePortalAdapter.CN_ROOT, _classes.get(SitexServicePortalAdapter.CN_ROOT));
	  			
	  			if (!classes.containsKey(result.ancestor) && _classes.containsKey(result.ancestor))
	  				classes.put(result.ancestor, _classes.get(result.ancestor));					
				}
					
  			classes.put(cn, result);
  			String wsdl = getClassWsdl(cn);
  			if (wsdl != null)
  				classWsdl.put(cn, wsdl);
  			
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
	
	protected StoredClassMeta requestClass(String cn,
																			Map<String, StoredClassMeta> classes,
																			Map<String, Map<String, String>> classAttrTabs,
																			Map<String, Map<String, String>> classAttrGroups,
																			Map<String, String> classWsdl,
																			Map<String, Integer> dictPermissions,
																			Map<String, Set<String>> dictionaries,
																			Map<String, String> classTypes,
																			List<UserProfile> profiles,
																			Map<String, Map<String, String>> collectionLinkClasses) throws SOAPException,
																									ParserConfigurationException,
																									SAXException, IOException,
																									XPathExpressionException,
																									DOMException, IonException, UnsupportedOperationException,
																									URISyntaxException, TransformerException {
		StoredClassMeta result = getClassFromCache(cn, classes, classWsdl, dictPermissions, classAttrGroups);
		if (result != null) 
			return result;
		
		logger.Info("Loading class " + cn);
		Map<String, Integer> attrOrder = new HashMap<String, Integer>();
		Map<String, Integer> attrTypes = new HashMap<String, Integer>();
		Map<String, Short> attrSize = new HashMap<String, Short>();
		Map<String, String> attrCaptions = new HashMap<String, String>();
		Map<String, String> attrRefClasses = new HashMap<String, String>();
		Object[] params = getClassMeta(cn, classAttrTabs, classAttrGroups, attrOrder, attrSize,
																	 attrTypes, attrCaptions, attrRefClasses, dictionaries, collectionLinkClasses);
		
		String wsdl = getClassWsdl(cn);

		if (wsdl != null/* && ((boolean)params[0] || !checkMeta)*/) {
			Object[] r = getWsdlReader(wsdl, new HashMap<String, String>());
			Document doc = (Document) r[0];
			XPath parser = (XPath) r[1];
			
// TODO По идее здесь надо брать только схему соответствующую targetNamespace			
			
			XPathExpression expr = parser.compile("/wsdl:definitions/wsdl:types/xsd:schema/xsd:include[1]/@schemaLocation");
			String schema = (String) expr.evaluate(doc, XPathConstants.STRING);

			result = loadClassFromSchema(getSchemaReader(schema,
																								 new HashMap<String, String>()), 
																cn,(String) params[1],(String) params[2], classes, classAttrTabs, 
																classAttrGroups, attrCaptions, attrTypes, attrOrder, attrSize, attrRefClasses, 
																classWsdl, dictPermissions, dictionaries, classTypes, profiles, collectionLinkClasses);
			if (result != null)
				classes.put(cn, result);
			return result;
		}
		return null;
	}

	private Object convertAttrValue(int type, String val) {
		if (type == MetaPropertyType.BOOLEAN.getValue())
			return val.equals("true");
		return val;
	}

	@SuppressWarnings("unchecked")
  private Object[] parseNode(Node n, ClassAssembler cm,
														 Map<String, StoredClassMeta> classes, List<DataUnit> units, 
														 Map<String, Set<String>> eager) 
																 throws XPathExpressionException, DOMException, 
																 SOAPException, ParserConfigurationException, 
																 SAXException, IOException, IonException, 
																 URISyntaxException, TransformerException {
		Map<String, Object> values = new HashMap<String, Object>();
		if (cm.key.equals("offlnSviId")){
			values.put("offlnSvVal", n.getTextContent());
			return new Object[] { null, values };
		}

		LinkedList<String> fileAttrs = new LinkedList<String>();
		
		Node attr = n.getFirstChild();
		String id = null;
		String actualClass = null;
		while (attr != null) {
			if (attr.getLocalName() != null) {
				String nm = /*escapeJavaKeyword(*/attr.getLocalName()/*)*/;
				if (nm.equals(cm.key))
					id = attr.getTextContent();

				if (cm.properties.containsKey(nm)) {
					StoredPropertyMeta pm = cm.properties.get(nm);
					String t = null;
					if (attr.getAttributes() != null
							&& attr.getAttributes().getNamedItem("xsi:type") != null) {
						t = attr.getAttributes().getNamedItem("xsi:type").getNodeValue();
						if (t.contains(":")) {
							String[] parts = t.split(":");
							t = parts[1];
						}
					}
					
					if (pm.type == MetaPropertyType.REFERENCE.getValue()){
						if (pm.ref_class.equals(t)){
							StoredClassMeta ncm = classes.get(pm.ref_class);
							if(ncm != null){
								ClassAssembler ncma =  new ClassAssembler(ncm, classes);
								Object[] r = parseNode(attr, ncma, classes, units, eager);
								if(eager.containsKey(cm.name) && eager.get(cm.name).contains(pm.name))
									units.add(new DataUnit((String) r[0], ncm.name, (Map<String, Object>) r[1]));
								values.put(nm, r[0]);
								
								
								// Злоебучий костыль для загрузки данных резолюции услуг
								
								if (cm.ancestor != null && cm.ancestor.equals("Petition")){
									if (nm.equals("resolution")){
										Map<String, Object> vm = (Map<String, Object>) r[1];
										if (vm.containsKey("description"))
											values.put("offlnResolutionDecision", vm.get("description"));
										
										if (vm.containsKey("docs")){
											Object test = vm.get("docs");
											if (test instanceof Collection){
  											Collection<Map<String, Object>> docs = (Collection<Map<String, Object>>)test;
  											for (Map<String, Object> doc: docs) {
  												String dcm = doc.get("__systemClass").toString();
  												String docWsdl = getClassWsdl(dcm);
  												if (docWsdl != null){
  													StoredClassMeta docClass = getStoredClassMeta(dcm, new HashMap<String, Set<String>>(), new HashMap<String, Map<String,String>>());
  													ClassAssembler docClassA = new ClassAssembler(docClass, classes);
  													Map<String, Object> f = new HashMap<String, Object>();
  													String docKey = docClass.key.toArray(new String[1])[0]; 
  													f.put(docKey, doc.get(docKey));
  													List<DataUnit> tmp = fetchData(docClassA, f, classes, eager, null);
  													
  													String[] docAttrs = parseResolutionDocAttrs(docClass);
  													
  													for (DataUnit docDu: tmp){
  														if (docDu.data.containsKey(docAttrs[0]))
  															values.put("offlnResolutionDocFile", docDu.data.get(docAttrs[0]));
  														
  														if (docDu.data.containsKey(docAttrs[1]))
  															values.put("offlnResolutionComment", docDu.data.get(docAttrs[1]));
  													}
  												}
  											}
											}
										}
									}
								}
							}
						} else
							values.put(nm, attr.getTextContent());
					} else if (pm.type == MetaPropertyType.STRUCT.getValue()) {
						if (pm.ref_class.equals(t)){
							StoredClassMeta ncm = classes.get(pm.ref_class);
							Object[] r = parseNode(attr, new ClassAssembler(ncm, classes), classes, units, eager);
							values.put(nm, r[1]);
						} else
							values.put(nm, attr.getTextContent());
					} else if (pm.type == MetaPropertyType.COLLECTION.getValue()/* && pm.items_class.equals(t)*/) {
						StoredClassMeta ncm = classes.get(pm.items_class);
						List<Map<String, Object>> elements = new LinkedList<Map<String, Object>>();
						Node el = attr.getFirstChild();
						while (el != null){
							if (el.getLocalName() != null){
								Object[] elo = parseNode(el, new ClassAssembler(ncm, classes), classes, units, eager);
								Map<String, Object> elm = (Map<String, Object>)elo[1];
								elm.put("__systemClass", (elo[2] != null)?elo[2]:pm.items_class);
								elements.add(elm);
							}
							el = el.getNextSibling();
						}
						values.put(nm, elements);
					} else if (pm.type == MetaPropertyType.FILE.getValue()) {
						Map<String, String> fd = new HashMap<String, String>();
						fd.put("fileContents", attr.getTextContent());
						values.put(nm, fd);
						fileAttrs.add(nm);
					} else 
						values.put(nm, convertAttrValue(pm.type, attr.getTextContent()));						
				} else if (nm.equals("systemClass")) {
					NodeList nl = attr.getChildNodes();
					for (int j = 0; j < nl.getLength(); j++){
						Node n1 = nl.item(j);
						if (n1.getLocalName() != null && n1.getLocalName().equals("name"))
							actualClass = n1.getTextContent();
					}
				}
			}
			attr = attr.getNextSibling();
		}
		
		for (String nm: fileAttrs){
			Map<String, String> fd = (Map<String, String>)values.get(nm);
			if (fd != null){
				if (cm.name.equals("cmsFile")){
  				if (values.containsKey("name"))
  					fd.put("fileName", values.get("name").toString());
  				if (values.containsKey("length"))
  					fd.put("fileSize", values.get("length").toString());
				} else {
  				if (values.containsKey(nm+"Name"))
  					fd.put("fileName", values.get(nm+"Name").toString());
  				if (values.containsKey(nm+"Mime"))
  					fd.put("fileType", values.get(nm+"Mime").toString());
  				if (values.containsKey(nm+"Size"))
  					fd.put("fileSize", values.get(nm+"Size").toString());
  				if (values.containsKey(nm+"Date"))
  					fd.put("fileDate", values.get(nm+"Date").toString());
				}
			}
		}

		return new Object[] { id, values, actualClass };
	}
	
	@SuppressWarnings("unchecked")
	public List<DataUnit> fetchData(ClassAssembler cm,
																	Map<String, Object> filter,
																	Map<String, StoredClassMeta> classes,
																	Map<String, Set<String>> eager,
																	Integer pageCount)
																																			 throws IonException {
		List<DataUnit> units = new LinkedList<DataUnit>();
		try {
  		logger.Info("loading objects of " + cm.name);
  		String wsdlAddr = getClassWsdl(cm.name);
  		String[] params = getEndPointAndBinding(wsdlAddr,
  																						new HashMap<String, String>());
  
  		double total = 0;
  		int page = 0;
  
  		SOAPElement offsetNode = null;
  		SOAPElement limitNode = null;
  
  		SOAPMessage request = messageFactory.createMessage();
  
  		String operName = "get" + capitalize(cm.name);
  
  		Object[] rqstParams = getFetchSOAPMsgParams(wsdlAddr, operName, cm.name);
  		if (rqstParams != null) {
  			String SOAPAction = (String) rqstParams[0];
  			String bodyNS = (String) rqstParams[1];
  			String condNS = (String) rqstParams[2];
  			String dataNS = (String) rqstParams[3];
  			Map<String, String> attrNS = (Map<String, String>)rqstParams[4];
  			String condsName = (String) rqstParams[5];
  
  			String bodyPrefix = "ppu";
  			String condPrefix = "cond";
  			String dataPrefix = "data";
  
  			String refAttrPrefix = "ns";
  
  			request.getSOAPPart().getEnvelope()
  				.addNamespaceDeclaration(bodyPrefix, bodyNS);
  						
  			if (condNS.equals(bodyNS))
  				condPrefix = bodyPrefix;
  			else
  				request.getSOAPPart().getEnvelope()
  				.addNamespaceDeclaration(condPrefix, condNS);
  				
  
  			if (dataNS.equals(condNS))
  				dataPrefix = condPrefix;
  			else
  				request.getSOAPPart().getEnvelope()
  				.addNamespaceDeclaration(dataPrefix, dataNS);
  				
  			
  			Map<String, String> attrPrefixes = new HashMap<String, String>();
  			int i = 1;
  			
  			for (String fnm: filter.keySet()){
  				if (attrNS.containsKey(fnm) && cm.properties.containsKey(fnm)){
  					String pref = refAttrPrefix + i;
  					attrPrefixes.put(fnm, pref);
  					request.getSOAPPart().getEnvelope()
  							 .addNamespaceDeclaration(pref, attrNS.get(fnm));
  					i++;
  				}
  			}
  						
  			request.getMimeHeaders().addHeader("SOAPAction", SOAPAction);
  			
  			addSecurityTokenHeader(request, sysLogin, sysToken);
  
  			SOAPBodyElement bl = request.getSOAPBody()
  																	.addBodyElement(new QName(bodyNS,
  																														condsName,
  																														bodyPrefix));
  			offsetNode = bl.addChildElement(new QName(bodyNS, "offset", bodyPrefix));
  			limitNode = bl.addChildElement(new QName(bodyNS, "limit", bodyPrefix));
  			limitNode.setTextContent(String.valueOf(50));
  			
  			SOAPElement el = bl.addChildElement(new QName(condNS,"CondOf"+cm.name,condPrefix));
  
  			SOAPElement dummy = el.addChildElement(new QName(dataNS,
  																											 cm.name,
  																											 dataPrefix));
  
  			for (Map.Entry<String, Object> kv: filter.entrySet()){
  				if (cm.properties.containsKey(kv.getKey())){
  					StoredPropertyMeta pm = cm.properties.get(kv.getKey());
    				SOAPElement a = dummy.addChildElement(new QName(dataNS, kv.getKey(), dataPrefix));
    				if (attrNS.containsKey(kv.getKey()) && (pm.ref_class != null)){
    					StoredClassMeta rcm = classes.get(pm.ref_class);
    					if (rcm != null){
    						SOAPElement id = a.addChildElement(new QName(attrNS.get(kv.getKey()), rcm.key.toArray(new String[1])[0], attrPrefixes.get(kv.getKey())));
    						id.setTextContent(kv.getValue().toString());
    					}
    				} else
    					a.setTextContent(kv.getValue().toString());
  				}
  			}
  			
  			if (cm.properties.containsKey("timeStamp")){
  				SOAPElement orderNode = bl.addChildElement(new QName(bodyNS, "order", bodyPrefix));
  				orderNode.setTextContent("timeStamp desc");				
  			}
  
  			SOAPMessage response;
  			if(pageCount == null)
  				pageCount = maxPageCount;
  			
  			while (page * 50 <= total && (pageCount == 0 || page < pageCount)) {
  				if (debug)
  					logger.Info("*");
  
  				if (offsetNode != null)
  					offsetNode.setTextContent(String.valueOf(50 * page));
  
  				request.saveChanges();
  				
  				response = request(new URL(params[0]), request);
  				
  				Document responseDoc = response.getSOAPBody().getOwnerDocument();
  
  				XPath respParser = getXPath(responseDoc);
  
  				XPathExpression expr10 = respParser.compile("//*[local-name() = '"
  						+ cm.name + "']");
  				NodeList classObjects = (NodeList) expr10.evaluate(responseDoc,
  																													 XPathConstants.NODESET);
  
  				for (int j = 0; j < classObjects.getLength(); j++) {
  					Object[] r = parseNode(classObjects.item(j), cm, classes, units, eager);
  					units.add(new DataUnit((String) r[0],
  																 cm.name,
  																 (Map<String, Object>) r[1]));
  				}
  
  				if (total == 0) {
  					XPathExpression total_expr = respParser.compile("//*[local-name() = 'total'][1]/text()");
  					total = (double) total_expr.evaluate(responseDoc,
  																							 XPathConstants.NUMBER);
  				}
  
  				if (offsetNode == null)
  					break;
  				page++;
  			}
  		}
  		// }
  		if (debug) {
  			logger.Info("");
  			logger.Info(total + " items loaded");
  		}
		// }
		} catch (DOMException | XPathExpressionException | ParserConfigurationException | SAXException 
				| IOException | SOAPException | URISyntaxException | TransformerException e) {
			throw new IonException(e);
		}
		return units;
	}
	
	private StoredClassMeta getStoredClassMeta(String cn, Map<String, StoredClassMeta> classes, Map<String, Set<String>> dictionaries, 
	                                           Map<String, Map<String, String>> collectionLinkClasses) 
	                                          		 throws XPathExpressionException, DOMException, SOAPException, 
	                                          		 ParserConfigurationException, SAXException, IOException, 
	                                          		 IonException, UnsupportedOperationException, 
	                                          		 URISyntaxException, TransformerException{
		Map<String, String> ct = new HashMap<String, String>();
		StoredClassMeta c = requestClass(cn, classes, new HashMap<String, Map<String,String>>(), 
		                                 new HashMap<String, Map<String, String>>(),
		                                 new HashMap<String, String>(), 
		                                 new HashMap<String, Integer>(), dictionaries, ct, new LinkedList<UserProfile>(),collectionLinkClasses);
		return c;
	}
		
	private StoredClassMeta getStoredClassMeta(String cn, Map<String, Set<String>> dictionaries, 
	                                           Map<String, Map<String, String>> collectionLinkClasses) 
	                                          		 throws XPathExpressionException, DOMException, SOAPException, 
	                                          		 ParserConfigurationException, SAXException, IOException, 
	                                          		 IonException, UnsupportedOperationException, 
	                                          		 URISyntaxException, TransformerException{
		Map<String, StoredClassMeta> classes = new HashMap<String, StoredClassMeta>();
		return getStoredClassMeta(cn, classes, dictionaries, collectionLinkClasses);
	}
	
	@SuppressWarnings("unchecked")
	private Object[] getPushSOAPMsgParams(String wsdlAddr, String operName, String className)
																																				 throws XPathExpressionException,
																																				 ParserConfigurationException,
																																				 SAXException,
																																				 IOException {
		Object[] params = getSOAPMsgParams(wsdlAddr, operName);

		String soapAction = (String) params[0];
		String bodyNS = (String) params[1];
		String msgElName = (String) params[2];
		String condNS = "";
		String dataNS = "";
		Map<String, String> argsNSs = new HashMap<String, String>();

		Object[] wsdlReader = getWsdlReader(wsdlAddr, new HashMap<String, String>());
		XPath xpath = (XPath) wsdlReader[1];
		Document doc = (Document) wsdlReader[0];

		XPathExpression expr = xpath.compile("//wsdl:types/xsd:schema/xsd:include/@schemaLocation");
		NodeList includes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < includes.getLength(); i++) {
			String url = includes.item(i).getNodeValue();
			Object[] condParams = getSOAPMsgElementParams(url, msgElName, bodyNS, className);
			if (condParams != null) {
				bodyNS = (String) condParams[0];
				condNS = (String) condParams[1];
				dataNS = (String) condParams[2];
				argsNSs = (Map<String, String>) condParams[3];					
				break;
			}
		}

		Object[] result = { soapAction, bodyNS, condNS, dataNS, argsNSs, msgElName};
		return result;
	}
	
	private int addAttrNodes(DataChange change, StoredClassMeta scm, 
	                          String ns, String prefix, SOAPElement parent, int nsCounter, String pmprefix,
	                          Map<String, StoredPropertyMeta> propsMap, Map<String, String> argsNSs, 
	                          Map<String, Set<String>> dictionaries, Map<String, Map<String, String>> collectionLinkClasses) 
								throws SOAPException, XPathExpressionException, DOMException, ParserConfigurationException, 
												SAXException, IOException, IonException, UnsupportedOperationException, 
												URISyntaxException, TransformerException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");		
		for(StoredPropertyMeta pm : scm.properties) {
			propsMap.put(pmprefix + pm.name, pm);
			if (pmprefix.isEmpty() && scm.key.equals(pm.name)) {
				SOAPElement idn = parent.addChildElement(new QName(ns, pm.name, prefix));
				idn.setTextContent(change.id);
			} else if (pm.type == MetaPropertyType.STRUCT.getValue()) {
				SOAPElement pr = parent.addChildElement(new QName(ns, pm.name, prefix));
				StoredClassMeta struct_cm = getStoredClassMeta(pm.ref_class, dictionaries, collectionLinkClasses);
				String structns = argsNSs.get(pm.name);
				pr.addNamespaceDeclaration("ns" + nsCounter, structns);
				nsCounter = addAttrNodes(change, struct_cm, structns, "ns" + nsCounter, pr, nsCounter, pmprefix + pm.name + "$",propsMap, argsNSs, dictionaries, collectionLinkClasses);
				if (!pr.hasChildNodes())
					parent.removeChild(pr);
				nsCounter++;				
			} else if (change.data.containsKey(pmprefix + pm.name)){
				Object value = change.data.get(pmprefix + pm.name);
				if (!((
							//pm.name.equals("pettOrg") || 
							//pm.name.equals("recipientOrg") || 
							pm.name.equals("systemClass") || 
							pm.name.equals("typeCode") ||
							pm.name.equals("ouid") ||
							pm.name.equals("date")
						) || (pm.name.equals("rqstPerson") || 
								change.action.equals(DataChangeType.CREATE.getValue())) && value == null)){
					SOAPElement pr = parent.addChildElement(new QName(ns, pm.name, prefix));
					if (value != null) {
						switch(MetaPropertyType.fromInt(pm.type)){
							case FILE: {
  								@SuppressWarnings("unchecked")
									Map<String, String> m = (Map<String, String>)value;
									pr.setTextContent(m.get("fileContents"));
									SOAPElement pr2;
									if (scm.name.equals("cmsFile")){
										if (m.containsKey("fileName")){
											pr2 = parent.addChildElement(new QName(ns, "name", prefix));
											pr2.setTextContent(m.get("fileName"));
										}
										if (m.containsKey("fileSize")){
											pr2 = parent.addChildElement(new QName(ns, "length", prefix));
											pr2.setTextContent(m.get("fileSize"));
										}
									} else {
										if (m.containsKey("fileName")){
											pr2 = parent.addChildElement(new QName(ns, pm.name+"Name", prefix));
											pr2.setTextContent(m.get("fileName"));
										}
										
										if (m.containsKey("fileSize")){
											pr2 = parent.addChildElement(new QName(ns, pm.name+"Size", prefix));
											pr2.setTextContent(m.get("fileSize"));
										}
										
										if (m.containsKey("fileType")){
											pr2 = parent.addChildElement(new QName(ns, pm.name+"Mime", prefix));
											pr2.setTextContent(m.get("fileType"));
										}
										
										if (m.containsKey("fileDate")){
											pr2 = parent.addChildElement(new QName(ns, pm.name+"Date", prefix));
											pr2.setTextContent(m.get("fileDate"));
										}
									}
	  					};break;							
							case DATETIME: {
								if (value instanceof Date)
									pr.setTextContent(format.format((Date)value));										
								else 
									pr.setTextContent(value.toString());
							}; break;
							case REFERENCE: {
								StoredClassMeta rcm = getStoredClassMeta(pm.ref_class, dictionaries, collectionLinkClasses);
								StoredPropertyMeta rpm = null;
								for(StoredPropertyMeta p : rcm.properties)
									if(rcm.key.equals(p.name))
										rpm = p;
								String refns = argsNSs.get(pm.name);
								
								pr.addNamespaceDeclaration("ns" + nsCounter, refns);
								SOAPElement ref_id = pr.addChildElement(new QName(refns, rpm.name, "ns" + nsCounter));
								
								ref_id.setTextContent(value.toString());
								nsCounter++;
							}; break;
							case COLLECTION: {
								/*
								StoredClassMeta items_class = getStoredClassMeta(pm.items_class, dictionaries);
								if(items_class.is_struct){										
									if (value instanceof Collection<?>){
										String pref = items_class.name.toLowerCase().replaceAll("_", "").substring(0, 3);
  									String colns = argsNSs.get(pm.name);
  									pr.addNamespaceDeclaration(pref, colns);
										for(Object structure: (Collection<Object>)value){
											SOAPElement struct_coll_prop = pr.addChildElement(new QName(colns, items_class.name, pref));
											Map<String, Object> v = (Map<String, Object>)structure;
											for(StoredPropertyMeta p : items_class.getProperties()){
	  										if(v.containsKey(p.name)) {
	  											SOAPElement struct_el = struct_coll_prop.addChildElement(new QName(colns, p.name, pref));
	  											struct_el.setTextContent(v.get(p.name).toString());
	  										}  											
	  									}
										}
									} else 
										pr.setTextContent(value.toString());
								}
								*/
							}break;
							default: {
								pr.setTextContent(value.toString());
							}
						}
					} else {
						/*
						pr.addAttribute(new QName("http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi"), "true");
						pr.setTextContent("");
						if (scm.name.equals("cmsFile")) {
							Object body = change.data.containsKey(pmprefix + "body")?change.data.get(pmprefix + "body"):null;
							if (body != null)
								parent.removeChild(pr);
						} else
						*/
							parent.removeChild(pr);
					}
				}
			}
		}	
		return nsCounter;
	}
	
	
	
	private StoredPropertyMeta findStoredPropertyMeta(String property, StoredClassMeta scm){
		for (StoredPropertyMeta spm : scm.properties){
			if(property.toLowerCase().equals(spm.name.toLowerCase())){
				return spm;
			}
		}
		return null;
	}
	
	private String requestForId(StoredClassMeta scm, String keyPropertyName,
	                            String keyPropertyValue, String[] responsePropertyNames) 
	              throws UnsupportedOperationException, XPathExpressionException, SOAPException, 
	              IOException, ParserConfigurationException, SAXException, DOMException, 
	              URISyntaxException, TransformerException, IonException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(keyPropertyName, keyPropertyValue);
		Map<String, StoredClassMeta> classes = new HashMap<String, StoredClassMeta>();
		
		List<DataUnit> tmp = fetchData(new ClassAssembler(scm, classes), filter, classes, _eagerLoads, null);

		for (DataUnit du: tmp){
			for (int i = 0; i < responsePropertyNames.length; i++) {
				if (du.data.containsKey(responsePropertyNames[i])){
					Object v = du.data.get(responsePropertyNames[i]);
					if (v != null)
						return v.toString();
				}
			}
		}
		return null;
	}
	
	private String findId(StoredClassMeta classMeta, StoredPropertyMeta linkClassProp, String id) 
			throws UnsupportedOperationException, XPathExpressionException, SOAPException, IOException, 
							ParserConfigurationException, SAXException, DOMException, URISyntaxException, 
							TransformerException, IonException{
		StoredPropertyMeta classKeyProp = findStoredPropertyMeta(classMeta.key.toArray(new String[1])[0],classMeta);
		Integer keyType = classKeyProp.type;
		if (keyType == MetaPropertyType.GUID.getValue())
			keyType = MetaPropertyType.STRING.getValue();
		if(keyType == linkClassProp.type)
			return id;
		else
			return requestForId(classMeta, classKeyProp.name, id, (linkClassProp.type == MetaPropertyType.INT.getValue())?new String[]{"ouid"}:new String[]{"guid","code","kladrCode"});
	}
	
	private DataChange createPutDataChange(DataChange parentDataChange, StoredClassMeta containerMeta, 
	                                       StoredClassMeta linkClassMeta, StoredClassMeta detailMeta, String detailId) 
	                                      		 throws UnsupportedOperationException, XPathExpressionException, SOAPException, 
	                                      		 				IOException, ParserConfigurationException, SAXException, DOMException, 
	                                      		 				URISyntaxException, TransformerException, IonException {
		DataChange dc = new DataChange(parentDataChange.author, DataChangeType.CREATE.getValue(), null, linkClassMeta.name, new HashMap<String, Object>());
		String toId = findId(detailMeta,findStoredPropertyMeta("toId",linkClassMeta), detailId);
		if (toId != null)
			dc.data.put("toId", toId);
		else
			logger.Warning("Не удалось найти связываемый объект класса "+detailMeta.name+" по ключу "+detailId);
		return dc;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, String> push(DataUnit unit, String token, 
	                                Map<String, Set<String>> dictionaries, 
	                                Map<String, Map<String, String>> collectionLinkClasses)
																								 throws IonException {
		try {
		if (unit instanceof DataChange){
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
			
			
  		String wsdlAddr = getClassWsdl(unit.className);
  		if (wsdlAddr != null) {
  			
  			String prefix = "";
  			
  			if (change.action.equals(DataChangeType.PUT.getValue())){
  				StoredClassMeta containerMeta = getStoredClassMeta(change.className, dictionaries, collectionLinkClasses);
  				
  				if(collectionLinkClasses.containsKey(change.className)){
  					Map<String, String> propsWithLinkClasses = collectionLinkClasses.get(change.className);
    				for(Entry<String, Object> dataEntry : change.data.entrySet()){
    					if(propsWithLinkClasses.containsKey(dataEntry.getKey())){
    						String linkClass = propsWithLinkClasses.get(dataEntry.getKey());
    						String idattr = null;
    						if(linkClass.startsWith("SXLink:")){
    							String[] linkClassSplit = linkClass.split(":");
    							linkClass = linkClassSplit[0];
    							idattr = linkClassSplit[1];
    						}
    						StoredClassMeta linkClassMeta = getStoredClassMeta(linkClass, dictionaries, collectionLinkClasses);
    						if (linkClassMeta == null){
    							logger.Warning("Не найден класс-связка " + linkClass +" для атрибута "+change.className+"."+dataEntry.getKey());
    							continue;
    						}
    							
    						Object tmp = dataEntry.getValue();
    						if (tmp == null)
    							continue;
    						if (tmp.getClass().isArray() || tmp instanceof Collection){
    							Object[] collectionItems = null;
    							if (tmp instanceof Collection)
    								collectionItems = ((Collection<Object>)tmp).toArray();
    							else
    								collectionItems = (Object[])tmp;
    							
    							// TODO оптимизировать - контейнер надо грузить один раз
    							
    							String fromId = findId(containerMeta,findStoredPropertyMeta("fromId",linkClassMeta), change.id);
    							
      						int counter = 0;
      						for(Object detail : collectionItems){
      							Map<String, String> detailInfo = (Map<String, String>) detail;
      							String detailClassname = detailInfo.get("className");
      							String detailId = detailInfo.get("id");
      							StoredClassMeta detailMeta = getStoredClassMeta(detailClassname, dictionaries, collectionLinkClasses);
      							DataChange dc = createPutDataChange(change, containerMeta, linkClassMeta, detailMeta, detailId);
      							if (dc.data.containsKey("toId")){
      								dc.data.put("fromId", fromId);
      								if(idattr != null){
      									dc.data.put("attr", idattr);
      									dc.data.put("num", counter);
      								}
      								try {
      									push(dc, token, dictionaries, collectionLinkClasses);
      									counter++;
      								} catch (Exception e) {
      									logger.Error("Ошибка помещения объекта в коллекцию!", e);
      								}
      							}
      						}
    						} else 
    							logger.Warning("В коллекцию "+change.className+"."+dataEntry.getKey()+" передано некорректное значение типа "+tmp.getClass().getCanonicalName()+".");
    					} else
    						logger.Warning("Для коллекции "+change.className+"."+dataEntry.getKey()+" не определено класса-связки.");
    				}
  					
  				} else 
  					logger.Warning("Для класса "+change.className+" не определено классов-связок.");
  				
  				return null;
  			}
  			
  			if (change.action.equals(DataChangeType.CREATE.getValue()))
  				prefix = "create";
  			else if (change.action.equals(DataChangeType.UPDATE.getValue()))
  				prefix = "update";
  			else if (!change.action.equals(DataChangeType.DELETE.getValue()))
  				throw new IonException("Inapropriate action specified");
  			
  		// if (debug)
  			logger.Info("pushing. ClassName: " + change.className + ". Author: " + change.author);
  			String[] params = getEndPointAndBinding(wsdlAddr,
  																							new HashMap<String, String>());  			
  			
  			SOAPMessage request = messageFactory.createMessage();
  			
  			String operName = prefix + capitalize(change.className);
  			
  			Object[] rqstParams = getPushSOAPMsgParams(wsdlAddr, operName, change.className);
  			if (rqstParams != null) {
  				
  				request.getSOAPPart().getEnvelope()
  				.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");  				
  				
  				String SOAPAction = (String) rqstParams[0];
  				String bodyNS = (String) rqstParams[1];
  				String condNS = (String) rqstParams[2];
  				String dataNS = (String) rqstParams[3];
  				Map<String, String> argsNSs = (Map<String, String>)rqstParams[4];
  				String condsName = (String) rqstParams[5];
  				
  				if ((condsName == null) || condsName.isEmpty())
  					throw new IonException("Не удалось определить имя SOAP-метода для операции " + change.action + " класса " + change.className + "!");
  				
  				String bodyPrefix = "ppu";
  				String condPrefix = "cond";
  				String dataPrefix = "data";
  				
  				request.getSOAPPart().getEnvelope()
  				.addNamespaceDeclaration(bodyPrefix, bodyNS);
  				
  				if (condNS.equals(bodyNS))
  					condPrefix = bodyPrefix;
  				else
  					request.getSOAPPart().getEnvelope()
  					.addNamespaceDeclaration(condPrefix, condNS);
  					
  
  				if (dataNS.equals(condNS))
  					dataPrefix = condPrefix;
  				else
  					request.getSOAPPart().getEnvelope()
  					.addNamespaceDeclaration(dataPrefix, dataNS);
  				
  				request.getMimeHeaders().addHeader("SOAPAction", SOAPAction);
  				
  				addSecurityTokenHeader(request, change.author, token);
  				
  				StoredClassMeta scm = getStoredClassMeta(change.className, dictionaries, collectionLinkClasses);
  								
  				SOAPBodyElement msg_elem = request.getSOAPBody().addBodyElement(new QName(bodyNS, condsName, bodyPrefix));
  				SOAPElement dummy = msg_elem.addChildElement(new QName(condNS, change.className, condPrefix));
  				
  				int nsCounter = 0;
  				  		
  				Map<String, StoredPropertyMeta> propsMap = new HashMap<String, StoredPropertyMeta>();
  				
  				addAttrNodes(change, scm, condNS, condPrefix, dummy, 
  				             nsCounter, "", propsMap, argsNSs, dictionaries, collectionLinkClasses);
  				
  				request.saveChanges();
  				SOAPMessage response = request(new URL(params[0]), request);
  				
  				Map<String, String> result = new HashMap<String, String>();
  				
  				Document responseDoc = response.getSOAPBody().getOwnerDocument();

  				XPath respParser = getXPath(responseDoc);

  				XPathExpression expr = respParser.compile("//*[local-name() = '" + change.className + "']/*");
  				
  				NodeList props = (NodeList)expr.evaluate(responseDoc, XPathConstants.NODESET);
  				
  				for (int i = 0; i < props.getLength(); i++){
  					Node prop = props.item(i);
  					if (propsMap.containsKey(prop.getLocalName())){
  						StoredPropertyMeta pm = propsMap.get(prop.getLocalName());
  						if (pm.type != MetaPropertyType.COLLECTION.getValue()){
  							if (pm.type == MetaPropertyType.REFERENCE.getValue()){
  								String key = null;
  								expr = respParser.compile("*[local-name() = 'guid'][1]/text()");
  								key = (String)expr.evaluate(prop, XPathConstants.STRING);
  								
  								if ((key == null) || key.isEmpty()){
  									expr = respParser.compile("*[local-name() = 'ouid'][1]/text()");
    								key = (String)expr.evaluate(prop, XPathConstants.STRING);
  								}
  								
  								if ((key == null) || key.isEmpty()){
  									expr = respParser.compile("*[local-name() = 'oid'][1]/text()");
  									key = (String)expr.evaluate(prop, XPathConstants.STRING);
  								}
  								
  								if ((key == null) || key.isEmpty()){
  									expr = respParser.compile("*[local-name() = 'kladrCode'][1]/text()");
  									key = (String)expr.evaluate(prop, XPathConstants.STRING);
  								}
  								
  								if ((key == null) || key.isEmpty()){
  									expr = respParser.compile("*[local-name() = 'code'][1]/text()");
  									key = (String)expr.evaluate(prop, XPathConstants.STRING);
  								}
  								result.put(pm.name, key);
  							} else {
  								result.put(pm.name, prop.getTextContent());
  							}
  						}
  					}
  				}
  				
  				if (result.containsKey(scm.key) && (unit.data.containsKey(scm.key) && unit.data.get(scm.key) != null)){
  					if (!unit.data.get(scm.key).equals(result.get(scm.key)))
  						logger.Warning("Целевая система изменила значение ключевого атрибута "+scm.key+" с значения "+unit.data.get(scm.key)+" на значение "+result.get(scm.key));
  				}
  				
  				return result;
  			}
  		}
		}
		} catch (DOMException | SOAPException | UnsupportedOperationException | IOException | XPathExpressionException 
				| ParserConfigurationException | SAXException | URISyntaxException | TransformerException e) {
			throw new IonException(e);
		}
		return null;
	}

	@Override
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
		Collection<StoredClassMeta> result = new LinkedList<StoredClassMeta>();
		
		for (String cn : names){
			if (classTypes.containsKey(cn) && !classTypes.get(cn).equals(SitexSoapRequestor.CT_OTHER)){
  			try {
  				StoredClassMeta m = this.requestClass(cn, classes, classAttrTabs, classAttrGroups, classWsdl, 
  				                       dictPermissions, dictionaries, classTypes, profiles, collectionLinkClasses);
  				if (m != null)
  					result.add(m);
  			} catch (SOAPException | XPathExpressionException 
  					| ParserConfigurationException | SAXException 
  					| IOException | UnsupportedOperationException | URISyntaxException | TransformerException e) {
  				e.printStackTrace(logger.Out());
  				logger.FlushStream(ILogger.ERROR);
  			}
			}
		}
		return result;
	}
}
