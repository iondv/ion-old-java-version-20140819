package ion.offline.adapters;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.logging.ILogger;
import ion.core.logging.IonLogger;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.integration.core.UserCredentials;
import ion.offline.net.AuthResult;
import ion.offline.net.ClassPermission;
import ion.offline.net.DataUnit;
import ion.offline.net.UserProfile;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

abstract public class BasicSitexSoapRequestor implements ISitexSoapRequestor {
	
	protected String									ppuUrl;

	protected String									userWsdl;

	protected ILogger									logger;
	
	protected Set<String>							eagerClasses;
	
	protected Set<String> dictionaryExcludeList = new HashSet<String>();
	
	protected Set<String> dictionaryIncludeList = new HashSet<String>();	

	protected URL										profilesEndpoint;

	protected boolean									debug				 = false;

	protected XPathFactory						xpathFactory;

	protected DocumentBuilderFactory	docFactory;

	protected MessageFactory					messageFactory;

	protected SOAPConnectionFactory		connectionFactory;

	protected Map<String, StoredClassMeta> _classes;
	
	protected Map<String, Set<String>> _eagerLoads = new HashMap<String, Set<String>>();
	
	protected Map<String, Object[]>		wsdlReaders;

	protected Map<String, String[]>		wsdlParams;

	protected Map<String, Object[]>		schemaReaders;
	
	protected String signaturesFetchWsdl;
	
	protected URL signaturesFetchEndpoint;
	
	protected String signaturesAttachWsdl;
	
	protected URL signaturesAttachEndpoint;
	
	protected String sysLogin;
	
	protected String sysToken;
	
	protected int soapConnectionTimeout = 5;
	
	protected int soapReadTimeOut = 10;

	protected int											maxPageCount	= 0;

	public static final String			 CT_FEDERAL		= "federal";

	public static final String			 CT_REGIONAL	 = "regional";

	public static final String			 CT_PETITION	 = "petition";

	public static final String			 CT_OTHER	 = "other";
	
	protected Map<String, String>	_classWsdl;

	@SuppressWarnings({ "serial" })
	protected Map<String, String>			classCaptions = new HashMap<String, String>() {
																									 {
																										 put(SitexServicePortalAdapter.CN_FEDERAL,
																												 "Федеральный сервис");
																										 put(SitexServicePortalAdapter.CN_REGIONAL,
																												 "Региональный сервис");
																										 put(SitexServicePortalAdapter.CN_PETITION, "Услуга");
																									 }
																								 };
																								 
	protected Map<String, String> signatureAttachType = new HashMap<String, String>();
	
	public BasicSitexSoapRequestor() throws IonException {
		xpathFactory = XPathFactory.newInstance();
		docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		try {
			messageFactory = MessageFactory.newInstance();
			connectionFactory = SOAPConnectionFactory.newInstance();
		} catch (SOAPException e) {
			throw new IonException(e);
		}
		wsdlReaders = new TreeMap<String, Object[]>();
		schemaReaders = new TreeMap<String, Object[]>();
		wsdlParams = new TreeMap<String, String[]>();
		_classes = new TreeMap<String, StoredClassMeta>();
		eagerClasses = new HashSet<String>();
		_classWsdl = new TreeMap<String, String>();
		logger = new IonLogger("sitex soap requestor");
	}
	
	protected String getClassWsdl(String cn) throws SOAPException,
																					 UnsupportedOperationException,
																					 IOException,
																					 XPathExpressionException {
		if (_classWsdl.containsKey(cn))
			return _classWsdl.get(cn);

		SOAPMessage msg = messageFactory.createMessage();
		msg.getSOAPPart().getEnvelope()
			 .addNamespaceDeclaration("user", "http://sys.smev.ru/xsd/user");
		msg.getMimeHeaders()
			 .addHeader("SOAPAction",
									"http://sys.smev.ru/xsd/user/User/getWSDLClassRequest");

		addSecurityTokenHeader(msg, sysLogin, sysToken);

		SOAPBodyElement bl = msg.getSOAPBody()
														.addBodyElement(new QName("http://sys.smev.ru/xsd/user",
																											"getWSDLClassRequest",
																											"user"));
		SOAPElement el = bl.addChildElement(new QName("http://sys.smev.ru/xsd/user",
																									"CondOfWSDLClass",
																									"user"));
		SOAPElement cl = el.addChildElement(new QName("http://sys.smev.ru/xsd/user",
																									"WSDLClass",
																									"user"));
		el = cl.addChildElement(new QName("http://sys.smev.ru/xsd/user",
																			"NAME",
																			"user"));
		el.setTextContent(cn);
		msg.saveChanges();
		msg = request(getProfilesEndpoint(), msg);

		XPath parser = xpathFactory.newXPath();
		XPathExpression expr = parser.compile("//*[local-name() = 'WSDLClass'][*[local-name() = 'NAME'][text() = '"
				+ cn + "']]/*[local-name() = 'wsdl']/text()");

		String wsdl = (String) expr.evaluate(msg.getSOAPBody(),
																				 XPathConstants.STRING);
		if (wsdl != null) {
			wsdl = ppuUrl + wsdl;
			_classWsdl.put(cn, wsdl);
			return wsdl;
		}
		_classWsdl.put(cn, null);
		return null;
	}

	protected Object[] getSOAPMsgParams(String wsdlAddr, String operName)
																																		 throws XPathExpressionException,
																																		 ParserConfigurationException,
																																		 SAXException,
																																		 IOException {
		String[] params = getEndPointAndBinding(wsdlAddr,
																						new HashMap<String, String>());
		Object[] wsdlReader = getWsdlReader(wsdlAddr, new HashMap<String, String>());
		XPath xpath = (XPath) wsdlReader[1];
		Document doc = (Document) wsdlReader[0];
		XPathExpression expr = xpath.compile("//wsdl:binding[@name='" + params[1]
				+ "']/wsdl:operation[@name='" + operName
				+ "']/wsdlsoap:operation/@soapAction");
		String soapAction = expr.evaluate(doc);
		expr = xpath.compile("//wsdl:binding[@name='" + params[1] + "']/@type");
		String portType = expr.evaluate(doc);
		portType = portType.substring(portType.indexOf(":") + 1);
		expr = xpath.compile("//wsdl:portType[@name='" + portType
				+ "']/wsdl:operation[@name='" + operName + "']/wsdl:input/@message");
		String messageType = expr.evaluate(doc);
		messageType = messageType.substring(messageType.indexOf(":") + 1);
		expr = xpath.compile("//wsdl:message[@name='" + messageType
				+ "']/wsdl:part/@element");
		String element = expr.evaluate(doc);
		String[] elementParts = element.split(":");

		String bodyNS = "";
		String bodyElName = "";

		if (elementParts.length > 1) {
			bodyNS = xpath.getNamespaceContext().getNamespaceURI(elementParts[0]);
			bodyElName = elementParts[1];
		} else
			bodyElName = elementParts[0];
		return new Object[] { soapAction, bodyNS, bodyElName };
	}

	private Object[] getSOAPClassParams(String schemaURL, String cn,
																			String prefix, int level, int depth)
																																					throws ParserConfigurationException,
																																					SAXException,
																																					IOException,
																																					XPathExpressionException {
		Object[] schemaReader = getSchemaReader(schemaURL,
																						new HashMap<String, String>());
		Document doc = (Document) schemaReader[0];
		XPath xpath = (XPath) schemaReader[1];
		return getSOAPClassParams(doc, xpath, cn, prefix, level, depth);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> parseSOAPFilterType(Node complexType,
																									XPath xpath, Document doc,
																									Set<String> imports,
																									String prefix, int level,
																									int depth)
																														throws XPathExpressionException,
																														ParserConfigurationException,
																														SAXException,
																														IOException {
		Map<String, String> result = new HashMap<String, String>();
		String tns = doc.getFirstChild().getAttributes()
										.getNamedItem("targetNamespace").getNodeValue();
		XPathExpression expr = xpath.compile("xsd:sequence/xsd:element");
		NodeList elements = (NodeList) expr.evaluate(complexType,
																								 XPathConstants.NODESET);
		for (int i = 0; i < elements.getLength(); i++) {
			Node element = elements.item(i);
			String name = element.getAttributes().getNamedItem("name").getNodeValue();
			String key = prefix + ((!prefix.isEmpty()) ? "/" : "") + name;
			if (element.getAttributes().getNamedItem("type") != null) {
				String type = element.getAttributes().getNamedItem("type")
														 .getNodeValue();
				String[] typeParts = type.split(":");
				if (typeParts.length > 1) {
					if (!typeParts[0].equals("xsd")) {
						result.put(key,
											 xpath.getNamespaceContext()
														.getNamespaceURI(typeParts[0]));
						expr = xpath.compile("//xsd:complexType[@name='" + typeParts[1]
								+ "']");
						Node typeNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
						if (typeNode != null) {
							if (level < depth)
								result.putAll(parseSOAPFilterType(typeNode, xpath, doc,
																									imports, key, level + 1,
																									depth));
						} else {
							if (level < depth) {
								for (String url : imports) {
									Object[] r1 = getSOAPClassParams(url, typeParts[1], key,
																									 level, depth);
									if (r1 != null)
										result.putAll((Map<String, String>) r1[1]);
								}
							}
						}

					} else if (typeParts[1].equals("dateTime")
							&& "timeStamp".equals(name))
						result.put(key, tns);
				} else
					result.put(key, tns);
			} else {
				result.put(key, tns);
				expr = xpath.compile("/xsd:complexType");
				Node typeNode = (Node) expr.evaluate(element, XPathConstants.NODE);
				if (typeNode != null) {
					if (level < depth)
						result.putAll(parseSOAPFilterType(typeNode, xpath, doc, imports,
																							key, level + 1, depth));
				}
			}
		}
		return result;
	}

	private Object[] getSOAPClassParams(Document doc, XPath xpath, String cn,
																			String prefix, int level, int depth)
																																					throws XPathExpressionException,
																																					ParserConfigurationException,
																																					SAXException,
																																					IOException {
		String tns = doc.getFirstChild().getAttributes()
										.getNamedItem("targetNamespace").getNodeValue();
		XPathExpression expr = xpath.compile("//xsd:complexType[@name='" + cn
				+ "']");
		Node dataTypeNode = (Node) expr.evaluate(doc, XPathConstants.NODE);

		if (dataTypeNode != null) {
			expr = xpath.compile("//xsd:import/@schemaLocation");
			NodeList imports = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			Set<String> schemas = new HashSet<String>();

			for (int i = 0; i < imports.getLength(); i++)
				schemas.add(imports.item(i).getNodeValue());

			Map<String, String> result = parseSOAPFilterType(dataTypeNode, xpath,
																											 doc, schemas, prefix,
																											 level, depth);
			Object[] r = { tns, result };
			return r;
		}
		return null;
	}

	protected Object[] getSOAPMsgElementParams(String schemaURL,
																					 String bodyElement, String bodyNS,
																					 String className)
																														throws XPathExpressionException,
																														ParserConfigurationException,
																														SAXException,
																														IOException {
		String condsNS = "";

		Object[] schemaReader = getSchemaReader(schemaURL,
																						new HashMap<String, String>());
		Document doc = (Document) schemaReader[0];
		String tns = doc.getFirstChild().getAttributes()
										.getNamedItem("targetNamespace").getNodeValue();
		if (bodyNS == null || bodyNS.isEmpty() || bodyNS.equals(tns)) {
			XPath xpath = (XPath) schemaReader[1];
			XPathExpression expr = xpath.compile("//xsd:element[@name='"
					+ bodyElement + "']/@type");
			String type = expr.evaluate(doc);
			if (type != null) {
				bodyNS = tns;

				String[] typeParts = type.split(":");
				if (typeParts.length > 1) {
					condsNS = xpath.getNamespaceContext().getNamespaceURI(typeParts[0]);
				} else {
					condsNS = tns;
				}

				Object[] res1 = getSOAPClassParams(doc, xpath, className, "", 1, 3);
				if (res1 != null) {
					Object[] result = { bodyNS, condsNS, res1[0], res1[1] };
					return result;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected Object[] getFetchSOAPMsgParams(String wsdlAddr, String operName,
																				 String className)
																													throws XPathExpressionException,
																													ParserConfigurationException,
																													SAXException,
																													IOException {
		Object[] params = getSOAPMsgParams(wsdlAddr, operName);

		String soapAction = (String) params[0];
		String bodyNS = (String) params[1];
		String condName = (String) params[2];
		String condNS = "";
		String dataNS = "";
		Map<String, String> argsNSs = null;

		Object[] wsdlReader = getWsdlReader(wsdlAddr, new HashMap<String, String>());
		XPath xpath = (XPath) wsdlReader[1];
		Document doc = (Document) wsdlReader[0];

		XPathExpression expr = xpath.compile("//wsdl:types/xsd:schema/xsd:include/@schemaLocation");
		NodeList includes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < includes.getLength(); i++) {
			String url = includes.item(i).getNodeValue();
			Object[] condParams = getSOAPMsgElementParams(url, condName, bodyNS,
																										className);
			if (condParams != null) {
				bodyNS = (String) condParams[0];
				condNS = (String) condParams[1];
				dataNS = (String) condParams[2];
				argsNSs = (Map<String, String>) condParams[3];
				break;
			}
		}

		Object[] result = { soapAction, bodyNS, condNS, dataNS, argsNSs, condName };
		return result;
	}	
	
	protected Object[] getWsdlReader(String wsdl, Map<String, String> namespaces)
																																						 throws ParserConfigurationException,
																																						 SAXException,
																																						 IOException,
																																						 XPathExpressionException {
		if (wsdlReaders.containsKey(wsdl))
			return wsdlReaders.get(wsdl);

		DocumentBuilder builder = docFactory.newDocumentBuilder();
		Document doc = builder.parse(wsdl);
		Object[] r = getWsdlReader(wsdl, doc, namespaces);
		wsdlReaders.put(wsdl, r);
		return r;
	}

	protected void setupXpath(XPath parser, Map<String, String> namespaces) {
		final Map<String, String> ns = namespaces;
		final Map<String, Set<String>> sn = new HashMap<String, Set<String>>();

		for (Map.Entry<String, String> p : namespaces.entrySet()) {
			if (!sn.containsKey(p.getValue()))
				sn.put(p.getValue(), new HashSet<String>());
			sn.get(p.getValue()).add(p.getKey());
		}

		parser.setNamespaceContext(new NamespaceContext() {

			public Iterator<String> getPrefixes(String namespaceURI) {
				if (sn.containsKey(namespaceURI))
					return sn.get(namespaceURI).iterator();
				return null;
			}

			public String getPrefix(String namespaceURI) {
				if (sn.containsKey(namespaceURI))
					return sn.get(namespaceURI).toArray(new String[1])[0];
				return null;
			}

			public String getNamespaceURI(String prefix) {
				if (prefix == null) {
					throw new IllegalArgumentException("No prefix provided!");
				} else if (ns.containsKey(prefix)) {
					return ns.get(prefix);
				} else {
					return XMLConstants.XML_NS_URI;
				}
			}
		});
	}
	
	private XPath bindXPath(XPath xpath, Document doc) {
		Map<String, String> namespaces = new HashMap<String, String>();
		NamedNodeMap attrs = doc.getFirstChild().getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			if (attr.getPrefix() != null && attr.getPrefix().equals("xmlns"))
				namespaces.put(attr.getLocalName(), attr.getNodeValue());
		}
		setupXpath(xpath, namespaces);
		return xpath;
	}	

	protected XPath getXPath(Document doc) {
		XPath parser = xpathFactory.newXPath();
		return bindXPath(parser, doc);
	}	

	protected void setupNamespaces(Document doc, Map<String, String> namespaces) {
		NamedNodeMap nodes = doc.getFirstChild().getAttributes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
				String[] parts = n.getNodeName().split(":");
				if (parts.length > 1) {
					if (parts[0].equals("xmlns")) {
						namespaces.put(parts[1], n.getNodeValue());
					}
				}
			}
		}
	}
	
	protected Object[] getSchemaReader(String schema, Map<String, String> namespaces)
																																								 throws ParserConfigurationException,
																																								 SAXException,
																																								 IOException {
		if (schemaReaders.containsKey(schema))
			return schemaReaders.get(schema);
		namespaces.put("", "http://www.w3.org/2001/XMLSchema");
		namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema");
		DocumentBuilder builder = docFactory.newDocumentBuilder();
		Document doc = builder.parse(schema);
		setupNamespaces(doc, namespaces);
		XPath parser = xpathFactory.newXPath();
		setupXpath(parser, namespaces);
		Object[] result = { doc, parser };
		schemaReaders.put(schema, result);
		return result;
	}

	protected Object[] getWsdlReader(String wsdl, Document doc,
																 Map<String, String> namespaces)
																																throws ParserConfigurationException,
																																SAXException,
																																IOException,
																																XPathExpressionException {
		XPath parser = xpathFactory.newXPath();
		namespaces.put("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		namespaces.put("wsdlsoap", "http://schemas.xmlsoap.org/wsdl/soap/");
		namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema");
		setupNamespaces(doc, namespaces);
		setupXpath(parser, namespaces);
		Object[] result = { doc, parser };
		return result;
	}
	
	protected String[] getEndPointAndBinding(String wsdl,
																				 Map<String, String> namespaces)
																																				throws ParserConfigurationException,
																																				SAXException,
																																				IOException,
																																				XPathExpressionException {
		if (wsdlParams.containsKey(wsdl))
			return wsdlParams.get(wsdl);
		Object[] r = getWsdlReader(wsdl, namespaces);
		Document doc = (Document) r[0];
		XPath parser = (XPath) r[1];
		XPathExpression expr = parser.compile("//wsdlsoap:address[1]/@location");
		String[] result = new String[2];
		result[0] = (String) expr.evaluate(doc, XPathConstants.STRING);
		expr = parser.compile("//wsdl:binding[1]/@name");
		result[1] = (String) expr.evaluate(doc, XPathConstants.STRING);
		wsdlParams.put(wsdl, result);
		return result;
	}	
	
	protected void addSecurityTokenHeader(SOAPMessage request, String user, String token) throws SOAPException {
		String wsseNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
		String wssePrefix = "wsse";
		String wsuNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
		String wsuPrefix = "wsu";
		
		SOAPHeader header = request.getSOAPHeader();				
		
		SOAPElement security = header.addChildElement(new QName(wsseNS, "Security", wssePrefix));
		security.addNamespaceDeclaration(wssePrefix, wsseNS);
		security.addAttribute(new QName(header.getNamespaceURI(),"mustUnderstand", header.getPrefix()), "1");
		
		SOAPElement usernameToken = security.addChildElement(new QName(wsseNS, "UsernameToken", wssePrefix));
		usernameToken.addNamespaceDeclaration(wsuPrefix, wsuNS);
		usernameToken.addAttribute(new QName(wsuNS, "Id", wsuPrefix), "UsernameToken-1592394766");
		
		SOAPElement username = usernameToken.addChildElement(new QName(wsseNS, "Username", wssePrefix));
		username.setTextContent(user);
		
		SOAPElement password = usernameToken.addChildElement(new QName(wsseNS, "Password", wssePrefix));
		password.addAttribute(new QName("Type"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
		//password.setTextContent("kDxpXqAPqZll9pc0bE9bxySGj1LO7BN+rnxJJQIQ2PpMnqwPsLZA20yEDpNxpLIoPyGo8L 9tokEV+4gm1m1zQc+ToSPqmjuxnsChvRZgb+KJBwWfAA7R8bp4R7vKl6xi3H+Vrv3ITVqe xZ3cv9MXZqY7jXCc1OMGNFzPk/uI/x0=");
		password.setTextContent(token);		
	}	
	
	protected SOAPMessage request(URL dest, SOAPMessage msg)
																								throws UnsupportedOperationException,
																												SOAPException,
																												IOException {
		if (debug) {
			logger.Debug("");
			logger.Debug("Logging request to: " + dest.toExternalForm());
			logger.Debug("********* Запрос ************");
			msg.writeTo(logger.Out());
			logger.FlushStream(ILogger.DEBUG);
		}
		SOAPMessage result = null;
		try {
			SOAPConnection connection = connectionFactory.createConnection();

			final int conto = soapConnectionTimeout * 1000;
			final int readto = soapReadTimeOut * 1000;

			URL endpoint = new URL(dest.getProtocol(),
														 dest.getHost(),
														 dest.getPort(),
														 dest.getFile(),
														 new URLStreamHandler() {
															 @Override
															 protected URLConnection openConnection(URL url)
																																							throws IOException {
																 URL urlcopy = new URL(url.toExternalForm());
																 URLConnection connection = urlcopy.openConnection();
																 connection.setConnectTimeout(conto);
																 connection.setReadTimeout(readto);
																 return connection;
															 }
														 });

			result = connection.call(msg, endpoint);
			connection.close();
		} catch (SOAPException e) {
			logger.Error("", e);
			throw e;
		}
		if (debug) {
			logger.Debug("********* Ответ ************");
			result.writeTo(logger.Out());
			logger.FlushStream(ILogger.DEBUG);
			logger.Debug("");
		}
		return result;
	}
	
	@Override
	public AuthResult[] authenticate(UserCredentials[] credentials)
																																 throws IonException {
		List<AuthResult> l = new LinkedList<AuthResult>();
		for (UserCredentials u : credentials) {
			try {
				SOAPMessage response = requestProfile(u.login, u.password);
				if (response.getSOAPBody().getFirstChild().getLocalName()
										.equals("getIonSXUserResponse")) {
					XPath parser = xpathFactory.newXPath();
					XPathExpression expr = parser.compile("//*[local-name() = 'HashPassword']/text()");
					String token = (String) expr.evaluate(response.getSOAPBody(),
																								XPathConstants.STRING);

					l.add(new AuthResult(u.login, true, "", token));
				} else if (response.getSOAPBody().getFirstChild().getLocalName()
													 .equals("Fault"))
					l.add(new AuthResult(u.login, false, response.getSOAPBody()
																											 .getFirstChild()
																											 .getFirstChild()
																											 .getNextSibling()
																											 .getTextContent(), ""));
			} catch (
							 SOAPException | XPathExpressionException
							 | UnsupportedOperationException e) {
				e.printStackTrace(logger.Out());
				logger.FlushStream(ILogger.ERROR);
			}
		}
		return l.toArray(new AuthResult[l.size()]);
	}
	
	protected URL getProfilesEndpoint() {
		if (profilesEndpoint == null) {
			try {
				String[] params = getEndPointAndBinding(this.userWsdl,
																								new HashMap<String, String>());

				this.profilesEndpoint = new URL(params[0]);
			} catch (
							 XPathExpressionException | ParserConfigurationException
							 | SAXException | IOException e) {
				logger.Error("", e);
			}
		}
		return profilesEndpoint;
	}
	
	protected SOAPMessage requestProfile(String login, String pwd) throws IonException {
		try {
  		SOAPMessage msg = messageFactory.createMessage();
  		msg.getSOAPPart().getEnvelope()
  			 .addNamespaceDeclaration("user", "http://sys.smev.ru/xsd/user");
  		msg.getSOAPPart().getEnvelope()
  			 .addNamespaceDeclaration("ppu", "http://sys.smev.ru/xsd/ppu");
  		msg.getSOAPPart().getEnvelope()
  			 .addNamespaceDeclaration("rqs", "http://sys.smev.ru/xsd/rqstMain");
  		msg.getMimeHeaders()
  			 .addHeader("SOAPAction",
  									"http://sys.smev.ru/xsd/user/User/getIonSXUserRequest");
  
  		// addSecurityTokenHeader(msg, sysLogin, sysToken);
  
  		SOAPBodyElement bl = msg.getSOAPBody()
  														.addBodyElement(new QName("http://sys.smev.ru/xsd/user",
  																											"getIonSXUserRequest",
  																											"user"));
  		SOAPElement el = bl.addChildElement(new QName("http://sys.smev.ru/xsd/user",
  																									"CondOfionSXUser",
  																									"user"));
  		SOAPElement usr = el.addChildElement(new QName("http://sys.smev.ru/xsd/user",
  																									 "ionSXUser",
  																									 "user"));
  		el = usr.addChildElement(new QName("http://sys.smev.ru/xsd/user",
  																			 "login",
  																			 "user"));
  		el.setTextContent(login);
  		if (pwd != null) {
  			el = usr.addChildElement(new QName("http://sys.smev.ru/xsd/user",
  																				 "password",
  																				 "user"));
  			el.setTextContent(pwd);
  		}
  		msg.saveChanges();
  		return request(getProfilesEndpoint(), msg);
		} catch (DOMException | SOAPException | UnsupportedOperationException | IOException e) {
			throw new IonException(e);
		}		
	}

	@Override
	public UserProfile getProfile(String login, String pwd, Set<String> classNames, Map<String, Set<String>> classOrgs, Map<String, String> classTypes, Map<String, String[]> classNav)
																												 throws IonException {
		XPath parser = xpathFactory.newXPath();
		XPathExpression findorgguid1, findorgguid2, findUser, findEmployee;
		try {
			findorgguid1 = parser.compile("//*[local-name() = 'egPosition']/*[local-name() = 'department']/*[local-name() = 'guid']");
			findorgguid2 = parser.compile("//*[local-name() = 'egPosition']/*[local-name() = 'department_GUID']");
			//(NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			findUser = parser.compile("//*[local-name() = 'ionSXUser'][1]/*[local-name() = 'idUser']");
			findEmployee = parser.compile("*[local-name() = 'employee']/*[local-name()='SD_Employee'][1]");
		
			SOAPMessage response = requestProfile(login, null);
			Node userNode = (Node)findUser.evaluate(response.getSOAPBody(), XPathConstants.NODE);
				
			Set<String> orgs = new HashSet<String>();
			if (userNode != null){
				
				Node employeeNode = (Node)findEmployee.evaluate(userNode, XPathConstants.NODE);
				
				UserProfile p = new UserProfile(login);
				NodeList l = userNode.getChildNodes();
				
				for (int i = 0; i < l.getLength(); i++){
					Node n = l.item(i);
					if (n.getLocalName().equals("first_name")){
						p.name = n.getTextContent();
					} else if (n.getLocalName().equals("second_name")){
						p.lastname = n.getTextContent();
					} else if (n.getLocalName().equals("login")){
						p.login = n.getTextContent();
					} else if (n.getLocalName().equals("guid")){
						p.properties.put("smev_id", n.getTextContent());
					}
				}
				
				l = employeeNode.getChildNodes();
				for (int i = 0; i < l.getLength(); i++){
					Node n = l.item(i);
					if (n.getLocalName().equals("first_name")){
						p.name = n.getTextContent();
					} else if (n.getLocalName().equals("second_name")){
						p.lastname = n.getTextContent();
					} else if (n.getLocalName().equals("guid")){
						p.properties.put("smev_id", n.getTextContent());
					}
				}
					
				NodeList orgguids = (NodeList)findorgguid1.evaluate(userNode, XPathConstants.NODESET);
				for (int i = 0; i < orgguids.getLength(); i++)
					orgs.add(orgguids.item(i).getTextContent());
				orgguids = (NodeList)findorgguid2.evaluate(userNode, XPathConstants.NODESET);
				for (int i = 0; i < orgguids.getLength(); i++)
					orgs.add(orgguids.item(i).getTextContent());
				p.properties.put("organisations", orgs);
					
				XPathExpression classNodes, find_class_type, find_provider_code, find_provider_name; 
				classNodes = parser.compile("//*[local-name() = 'SecObj']");
				find_class_type = parser.compile("*[local-name() = 'ClassType']/text()");
				find_provider_code = parser.compile("*[local-name() = 'A_PROVIDER']/*[local-name() = 'provider'][1]/text()");
				find_provider_name = parser.compile("*[local-name() = 'A_PROVIDER']/*[local-name() = 'name'][1]/text()");
				l = (NodeList)classNodes.evaluate(response.getSOAPBody(), XPathConstants.NODESET);
					
				for (int i = 0; i < l.getLength(); i++){
					Node n = l.item(i);
					String className = null;
					String classType = null;
					String provider_code = "";
					String provider_title = "";
					int permissions = 0;
					Node n2 = n.getFirstChild();
					while (n2 != null) {
						if (n2.getLocalName().equals("code")) {
							className = n2.getTextContent();
						} else if (n2.getLocalName().equals("C") && n2.getTextContent().equals("true")) {
							permissions = permissions | ClassPermission.CREATE.getValue();
						} else if (n2.getLocalName().equals("R") && n2.getTextContent().equals("true")) {
							permissions = permissions | ClassPermission.READ.getValue();
						} else if (n2.getLocalName().equals("U") && n2.getTextContent().equals("true")) {
							permissions = permissions | ClassPermission.UPDATE.getValue();
						} else if (n2.getLocalName().equals("D") && n2.getTextContent().equals("true")) {
							permissions = permissions | ClassPermission.DELETE.getValue();
						}
						n2 = n2.getNextSibling();
					}

					classType = (String)find_class_type.evaluate(n,XPathConstants.STRING);
					provider_code = (String)find_provider_code.evaluate(n,XPathConstants.STRING);
					provider_title = (String)find_provider_name.evaluate(n,XPathConstants.STRING);						
						
					if (className != null && className.matches("[a-zA-Z][a-zA-Z0-9_\\$]*") && !className.equals("baseMZ")){
						classNames.add(className);
						if (classType != null){
							if (provider_code != null)
								classNav.put(className, new String[]{
										(classType + (provider_code.isEmpty()?"":"|" + provider_code)),
										provider_title
								});
								classTypes.put(className,  classType);
						}
						p.access.put(className, permissions);
							
						if (!orgs.isEmpty()){
							if (!classOrgs.containsKey(className))
								classOrgs.put(className, new HashSet<String>());
							classOrgs.get(className).addAll(orgs);
						}
					}
				}
				return p;
			}
		} catch (SOAPException | UnsupportedOperationException | XPathExpressionException e) {
				logger.Error("Ошибка при получении профиля пользователя " + login, e);
		}
		return null;
	}
	
	private DataUnit parseTemplate(String className, Node tplNode, XPath parser, int counter) throws XPathExpressionException {
		XPathExpression expr = parser.compile("Active");
		Node active = (Node)expr.evaluate(tplNode, XPathConstants.NODE);
		if (active != null)
			if (!"true".equals(active.getTextContent()))
				return null;
		
		DataUnit unit = new DataUnit();

		expr = parser.compile("XPathValidator/Condition");
		
		NodeList templateConditions = (NodeList) expr.evaluate(tplNode, XPathConstants.NODESET);
		
		List<String[]> cl = new LinkedList<String[]>();
		
		for (int j = 0; j < templateConditions.getLength(); j++){
			Node c = templateConditions.item(j);
			String[] ca = new String[2];
			ca[0] = c.getTextContent();
			ca[1] = "";
			if (c.getNextSibling() != null)
				ca[1] = c.getNextSibling().getTextContent();
			cl.add(ca);
		}
		
		unit.data.put("conditions", cl);
				
		expr = parser.compile("Xslt/VelocityParams/Param");
		NodeList templateParams = (NodeList) expr.evaluate(tplNode, XPathConstants.NODESET);  			
		
		List<String> pl = new LinkedList<String>();
		
		for (int j = 0; j < templateParams.getLength(); j++)
			pl.add(templateParams.item(j).getTextContent());

		unit.data.put("params", pl);
		
		expr = parser.compile("Xslt/Content/text()");
		unit.data.put("template", (String) expr.evaluate(tplNode, XPathConstants.STRING));
				
		unit.className = className;
		if (tplNode.getAttributes().getNamedItem("operation") != null)
			unit.id = tplNode.getAttributes().getNamedItem("operation").getNodeValue() + "." + counter + ".tpl";
		else
			unit.id = "send." + counter + ".tpl";
		return unit;
	}
	
	protected URL getSignaturesFetchEndpoint() {
		if (signaturesFetchEndpoint == null) {
			try {
				String[] params = getEndPointAndBinding(this.signaturesFetchWsdl,
																								new HashMap<String, String>());

				this.signaturesFetchEndpoint = new URL(params[0]);
			} catch (
							 XPathExpressionException | ParserConfigurationException
							 | SAXException | IOException e) {
				logger.Error("", e);
			}
		}
		return signaturesFetchEndpoint;
	}
	
	protected URL getSignaturesAttachEndpoint() {
		if (signaturesAttachEndpoint == null) {
			try {
				String[] params = getEndPointAndBinding(this.signaturesAttachWsdl,
																								new HashMap<String, String>());

				this.signaturesAttachEndpoint = new URL(params[0]);
			} catch (
							 XPathExpressionException | ParserConfigurationException
							 | SAXException | IOException e) {
				logger.Error("", e);
			}
		}
		return signaturesAttachEndpoint;
	}		

	@Override
	public Collection<DataUnit> requestSignatureTemplate(String className, String digestTplEncoding) throws IonException {
		XPath parser = null;
		SOAPElement el = null;
		SOAPMessage msg = null;
		Collection<DataUnit> result = new LinkedList<DataUnit>();
		try {
  		msg = messageFactory.createMessage();
  		msg.getSOAPPart().getEnvelope()
  			 .addNamespaceDeclaration("ion","http://xsd.smev.ru/ppu/ionProcessingService");
  		
  		msg.getMimeHeaders()
  			 .addHeader("SOAPAction",
  									"getSoapExtensionRequest");
  		
			addSecurityTokenHeader(msg, sysLogin, sysToken);
  		  		
  		SOAPBodyElement bl = msg.getSOAPBody()
  														.addBodyElement(new QName("http://xsd.smev.ru/ppu/ionProcessingService",
  																											"getRequestReq",
  																											"ion"));
  
  		el = bl.addChildElement(new QName("http://xsd.smev.ru/ppu/ionProcessingService",
  																			"clsName",
  																			"ion"));
  
  		parser = xpathFactory.newXPath();
			el.setTextContent(className);
			msg.saveChanges();
			SOAPMessage response = request(getSignaturesFetchEndpoint(), msg);
			  			
			XPathExpression expr = parser.compile("//*[local-name() = 'generatedData']/text()");
			
			String body = new String(DatatypeConverter.parseBase64Binary((String) expr.evaluate(response.getSOAPBody(), XPathConstants.STRING)));
			
			if (body.isEmpty()){
  			expr = parser.compile("//*[local-name() = 'faultstring']/text()");
  			String fault = (String) expr.evaluate(response.getSOAPBody(), XPathConstants.STRING);
  			if (!fault.isEmpty())
  				throw new IonException(fault);
			}
			
			if (!body.isEmpty() && !body.equals("NO_CONTENT_GENERATED")){
  			Document bodyDoc = docFactory.newDocumentBuilder().parse(new InputSource(new StringReader(body)));
  			expr = parser.compile("/RequestOuter/TransportType/SoapExtension/Templates/Template");
  			
  			NodeList templateNodes = (NodeList) expr.evaluate(bodyDoc, XPathConstants.NODESET); 
  			
  			if (templateNodes.getLength() == 0){
  				expr = parser.compile("RequestOuter/TransportType/SoapExtension/WsServiceExtension/RequestTemplates/Template");
  				templateNodes = (NodeList) expr.evaluate(bodyDoc, XPathConstants.NODESET); 				
  			}
  			
  			Map<String, String> attrs = new HashMap<String, String>();
  			
  			expr = parser.compile("/RequestOuter/Signature/Type/text()");
  			String sigType = (String)expr.evaluate(bodyDoc, XPathConstants.STRING);
  			if (sigType.isEmpty())
  				sigType = "xml";
  			
  			attrs.put("signatureType", sigType);
  			
  			expr = parser.compile("/RequestOuter/Signature/Path/text()");
  			String sigPath = (String)expr.evaluate(bodyDoc, XPathConstants.STRING);
  			if (sigPath.isEmpty())
  				sigPath = "//*[local-name()='AppData']";

  			attrs.put("signaturePath", sigPath);
 			    			
  			expr = parser.compile("/RequestOuter/Signature/Place/text()");
  			String sigPlace = (String)expr.evaluate(bodyDoc, XPathConstants.STRING);

  			attrs.put("signaturePlace", sigPlace);
  			
  			if (signatureAttachType.containsKey(className))
  				attrs.put("attachType", signatureAttachType.get(className));
  			else
  				attrs.put("attachType", "preparedData");
 			    			
    		for (int i = 0; i < templateNodes.getLength(); i++) {
    			Node tpl = templateNodes.item(i);
    			DataUnit unit = parseTemplate(className, tpl, parser, i);
    			if (unit != null){
    				unit.data.put("attrs", attrs);
    				result.add(unit);
    			}
  			}
			}
		} catch (Exception e) {
			logger.Error("Ошибка при получении шаблона ЭП для класса " + className, e);
		}
		return result;
	}
	
	@Override
	public StoredClassMeta requestClass(String cn,
																			Map<String, StoredClassMeta> classes,
																			Map<String, String> classWsdl)
																																					 throws IonException {
		Collection<String> nms = new LinkedList<String>();
		nms.add(cn);
		
		Collection<StoredClassMeta> cl = requestClasses(nms, /*null,*/ classes,
												new HashMap<String, Map<String, String>>(),
												new HashMap<String, Map<String, String>>(),
												new HashMap<String, Map<String,Collection<String>>>(),
												classWsdl,
												new HashMap<String, Integer>(),
												new HashMap<String, Set<String>>(),
												new HashMap<String, String>(),
												new LinkedList<UserProfile>(),
												new HashMap<String, Map<String, String>>());
		for (StoredClassMeta c: cl)
			return c;
		
		return null;
	}
	
	@Override
	public List<DataUnit> fetchData(ClassAssembler cm,
																	Map<String, Object> filter,
																	Map<String, StoredClassMeta> classes,
																	Integer pageCount) throws IonException {
		return fetchData(cm, filter, classes, _eagerLoads, pageCount);
	}
	
	protected String[] parseResolutionDocAttrs(StoredClassMeta cm){
		String[] result = new String[2];
		for (StoredPropertyMeta p : cm.properties){
			String pname = p.name.toLowerCase();
			String pcaption = p.caption.toLowerCase();
			if(p.type == MetaPropertyType.FILE.getValue()){
				result[0] = p.name;
			} else if (p.type == MetaPropertyType.TEXT.getValue() && 
					(
							pname.contains("komment") ||
							pname.contains("comment") ||
							pcaption.contains("коммент")
					)){
				result[1] = p.name;
			}
			
			if (result[0] != null && result[1] != null)
				break;
		}		
		return result;
	}	
	
	public String[] fetchResolutionDescDoctype(String doctypeOuid, 
	                                           Map<String, Set<String>> dictionaries, 
	                                           Map<String, Map<String, String>> collectionLinkClasses) 
	                                          		throws IonException {
		String[] result = new String[3];
		try {
  		SOAPMessage request = messageFactory.createMessage();
  
  		String endPoint = ppuUrl+"/smvservices/smvPpuService.jws"; 
  		String SOAPAction = "http://sys.smev.ru/xsd/smevppu/smvPpuService/getSmevResolutionDescRequest";
  		
  		String smevNs = "http://sys.smev.ru/xsd/smevppu";
  		
  		request.getSOAPPart().getEnvelope().addNamespaceDeclaration("smev", smevNs);
  		request.getMimeHeaders().addHeader("SOAPAction", SOAPAction);
  
  		SOAPBodyElement bl = request.getSOAPBody().addBodyElement(new QName(smevNs,"getSmevResolutionDescRequest","smev"));
  		SOAPElement el = bl.addChildElement(new QName(smevNs, "CondOfsmevResolutionDesc", "smev"));
  		el = el.addChildElement(new QName(smevNs, "smevResolutionDesc", "smev"));
  		el = el.addChildElement(new QName(smevNs, "ouid", "smev"));
  		el.setTextContent(doctypeOuid);
  		
  		request.saveChanges();
  		
  		SOAPMessage response = request(new URL(endPoint), request);		
  		
  		Document responseDoc = response.getSOAPBody().getOwnerDocument();
  
  		XPath respParser = getXPath(responseDoc);
  
  		XPathExpression expr = respParser.compile("//*[local-name() = 'smevResolutionDesc'][1]/*[local-name() = 'docTypes']/*[local-name() = 'smevDocType'][1]/*[local-name() = 'clsImpl']/*[local-name() = 'name']/text()");
  		result[0] = (String) expr.evaluate(responseDoc, XPathConstants.STRING);
  		
  		if (result[0] != null) {
  			StoredClassMeta docScm = requestClass(result[0], new HashMap<String, StoredClassMeta>(), new HashMap<String, String>());
  			String[] attrs = parseResolutionDocAttrs(docScm);
  			result[1] = attrs[0];
  			result[2] = attrs[1];
  		}
		} catch (DOMException | SOAPException | UnsupportedOperationException | IOException 
				| XPathExpressionException e) {
			throw new IonException(e);
		}
		return result;
	}	
	
	protected Map<String, String> fetchResolutionDesc(String className) throws SOAPException, UnsupportedOperationException, MalformedURLException, IOException, XPathExpressionException {
		Map<String, String> result = new LinkedHashMap<String, String>();
		
		SOAPMessage request = messageFactory.createMessage();

		String endPoint = ppuUrl+"/smvservices/smvPpuService.jws"; 
		String SOAPAction = "http://sys.smev.ru/xsd/smevppu/smvPpuService/getSmevResolutionDescRequest";
		
		String smevNs = "http://sys.smev.ru/xsd/smevppu";
		
		request.getSOAPPart().getEnvelope().addNamespaceDeclaration("smev", smevNs);
		request.getMimeHeaders().addHeader("SOAPAction", SOAPAction);

		SOAPBodyElement bl = request.getSOAPBody().addBodyElement(new QName(smevNs,"getSmevResolutionDescRequest","smev"));
		SOAPElement el = bl.addChildElement(new QName(smevNs, "CondOfsmevResolutionDesc", "smev"));
		el = el.addChildElement(new QName(smevNs, "smevResolutionDesc", "smev"));
		el = el.addChildElement(new QName(smevNs, "class", "smev"));
		el.setTextContent(className);
		
		request.saveChanges();
		
		SOAPMessage response = request(new URL(endPoint), request);		
		
		Document responseDoc = response.getSOAPBody().getOwnerDocument();

		XPath respParser = getXPath(responseDoc);

		XPathExpression expr10 = respParser.compile("//*[local-name() = 'smevResolutionDesc']");
		NodeList descs = (NodeList) expr10.evaluate(responseDoc, XPathConstants.NODESET);
		
		for (int i = 0; i < descs.getLength(); i++) {
			XPathExpression expr = respParser.compile("*[local-name() = 'ouid']/text()");
			String ouid = (String) expr.evaluate(descs.item(i), XPathConstants.STRING);
			//expr = respParser.compile("*[local-name() = 'docTypes']/*[local-name() = 'smevDocType'][1]/*[local-name() = 'code']/text()");
			//String resolType = (String) expr.evaluate(descs.item(i), XPathConstants.STRING);
			expr = respParser.compile("*[local-name() = 'name']/text()");
			String name = (String) expr.evaluate(descs.item(i), XPathConstants.STRING);
			result.put(ouid, name.toLowerCase().contains("положит")?"Положительное":"Отрицательное");
		}
		
		return result;
	}		

	@Override
	public void setUserWsdl(String userWsdl) {
		this.userWsdl = userWsdl;
	}

	@Override
	public void setPpuUrl(String ppuUrl) {
		this.ppuUrl = ppuUrl;
	}
	
	@Override
	public void setSignaturesFetchWsdl(String url) {
		this.signaturesFetchWsdl = url;
	}

	@Override
	public void setSignaturesAttachWsdl(String url) {
		this.signaturesAttachWsdl = url;
	}
	
	@Override
	public void setMaxPageCount(int maxPageCount) {
		this.maxPageCount = maxPageCount;
	}

	@Override
	public void setEagerClasses(Set<String> ec) {
		this.eagerClasses = ec;
	}

	@Override
	public void setSysLogin(String sysLogin) {
		this.sysLogin = sysLogin;
	}

	@Override
	public void setSysToken(String sysToken) {
		this.sysToken = sysToken;
	}
	
	@Override
	public void setSoapConnectionTimeout(int soapConnectionTimeout) {
		this.soapConnectionTimeout = soapConnectionTimeout;
	}
	
	@Override
	public void setSoapReadTimeOut(int soapReadTimeOut) {
		this.soapReadTimeOut = soapReadTimeOut;
	}
	
	@Override
	public void setDictionaryExcludeList(Set<String> dictionaryExcludeList) {
		this.dictionaryExcludeList = dictionaryExcludeList;
	}
	
	@Override
	public void setDictionaryIncludeList(Set<String> dictionaryIncludeList) {
		this.dictionaryIncludeList = dictionaryIncludeList;
	}
	
	@Override
	public void setDebug(boolean v) {
		this.debug = v;
	}
	
	@Override
	public void setSignatureAttachType(Map<String, String> attachTypes){
		signatureAttachType = attachTypes;
	}	
}
