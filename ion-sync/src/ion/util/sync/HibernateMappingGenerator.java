package ion.util.sync;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

public class HibernateMappingGenerator extends JPAEntityGenerator {
	
	private int batchSize = 20;
	
	@Override
	protected void generateImports(OutputStreamWriter w, StoredClassMeta cm,
																 File metaDirectory, boolean needGeration,
																 boolean needLob, boolean needRefs,
																 boolean needOneToMany, boolean needManyToMany)
																																							 throws IOException {
		super.generateImports(w, cm, metaDirectory, needGeration, needLob,
													needRefs, needOneToMany, needManyToMany);
		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		if (!isSuper && useDiscriminator || needRefs || needOneToMany || needManyToMany){
			w.write("import org.hibernate.annotations.FetchMode;\n");
			w.write("import org.hibernate.annotations.Fetch;\n");
			w.write("import org.hibernate.annotations.BatchSize;\n");
		}
	}

	@Override
	protected void generateClassMappingAnnotations(OutputStreamWriter w,
																								 StoredClassMeta cm,
																								 File metaDirectory)
																																		throws IOException, IonException {
		super.generateClassMappingAnnotations(w, cm, metaDirectory);
		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		if (!isSuper && useDiscriminator) {
				w.write("@org.hibernate.annotations.Table(appliesTo=\""
					+ dbSanitise(cm.name, "t") + "\", fetch=FetchMode.SELECT)\n");
				w.write("@BatchSize(size=" + batchSize + ")\n");
		}
	}
	
	@Override
	protected void generateReferenceAnnotations(OutputStreamWriter w,
																							StoredPropertyMeta pm,
																							StoredClassMeta cm,
																							File metaDirectory,
																							Object[] params) throws IOException, IonException{
		super.generateReferenceAnnotations(w, pm, cm, metaDirectory, params);
		if (((pm.eager_loading != null) && pm.eager_loading))
			w.write("@Fetch(FetchMode.JOIN)\n");			
		else
			w.write("@Fetch(FetchMode.SELECT)\n");
		w.write("@BatchSize(size=" + batchSize + ")\n");
	}
	
	@Override
	protected void generateCollectionAnnotations(OutputStreamWriter w,
																							 StoredPropertyMeta pm,
																							 StoredClassMeta cm,
																							 File metaDirectory,
																							 Object[] params)
																															 throws IOException,
																															 IonException {
		super.generateCollectionAnnotations(w, pm, cm, metaDirectory, params);
		if (((pm.eager_loading != null) && pm.eager_loading))
			w.write("@Fetch(FetchMode.JOIN)\n");			
		else
			w.write("@Fetch(FetchMode.SELECT)\n");
		w.write("@BatchSize(size=" + batchSize + ")\n");
	}

	private String getMappingType(StoredPropertyMeta pm, File metaDirectory) throws IonException{
		switch (MetaPropertyType.fromInt(pm.type)){
			case BOOLEAN:return "boolean";
			case DATETIME:return "date";
			case DECIMAL:return "big_decimal";
			case INT:
			case SET:return "integer";
			case REAL:return "double";
			case GUID:return "string";
			case FILE:
			case IMAGE:
			case PASSWORD:
			case STRING:
			case URL: return "string";
			case STRUCT:			
			case HTML:
			case TEXT: return "text";
			case REFERENCE:{
				StoredClassMeta kh = getKeyHolder(pm.ref_class, metaDirectory);
				StoredPropertyMeta kp = null;
				for (StoredPropertyMeta p: kh.properties)
					if (p.name.equals(kh.key))
						kp = p;
				return getMappingType(kp, metaDirectory);
			}
			default:return "character";
		}
	}	
	
	private Element formElement(StoredPropertyMeta pm, Object[] params, boolean is_key, Document doc, File metaDirectory) throws IonException{
		Element newElement;
		if (is_key){
			newElement = doc.createElement("id");
			newElement.setAttribute("column", dbSanitise(pm.name,"f"));
		} else {
			newElement = doc.createElement("property");
		}
		
		newElement.setAttribute("type", getMappingType(pm, metaDirectory));
		
		newElement.setAttribute("name",attrName(pm.name));
		if (!is_key){
			Element column = doc.createElement("column");
			column.setAttribute("name",dbSanitise(pm.name,"f"));
			if((Short)params[1] > 0){
				if((Boolean)params[4]){
					column.setAttribute("length",((Short)params[1]).toString());
				} else {
					column.setAttribute("precision",((Short)params[1]).toString());
					if((Short)params[2] > 0) {
						column.setAttribute("scale",((Short)params[2]).toString());
					}
				}
			}
			if (!pm.nullable)
				column.setAttribute("not-null", "true");
			newElement.appendChild(column);				
		}
		return newElement;
	}
	
	private Element pmToHb(StoredPropertyMeta pm, StoredClassMeta cm, StoredClassMeta kh, File metaDirectory, Document doc) throws IonException{
		Object[] prop_type;
		
		if (pm.type == null){
			throw new IonException("Не указан тип атрибута \""+cm.caption+"."+ pm.caption+"\".");
		}
		if (pm.type.intValue() != MetaPropertyType.COLLECTION.getValue()){
			prop_type = null;
			try {
				prop_type = parseType(pm, cm.is_struct, metaDirectory);
			} catch (IonException e){
				throw new IonException("Не удалось определить тип атрибута \""+cm.caption+"."+pm.caption+"\": "+e.getMessage());
			}
		
			if (prop_type == null){
				throw new IonException("Недопустимый тип у атрибута \""+cm.caption+"."+ pm.caption+"\".");
			}
		/*
			if ((prop_type != null) && (((String)prop_type[3]).length() > 0))
				types.add((String)prop_type[3]); 
		*/
			if (pm.size != null && pm.size > 0)
				prop_type[1] = pm.size;  

			if (pm.decimals != null && pm.decimals > 0)
				prop_type[2] = pm.decimals;  
		
			if (pm.name.equals(kh.key)){
				Element id = formElement(pm, prop_type, true, doc, metaDirectory);
				if (needGeneration(pm, cm)){
					Element generator = doc.createElement("generator");
					generator.setAttribute("class", "native");
					id.appendChild(generator);
				}
				return id;
			} else
				return formElement(pm, prop_type, false, doc, metaDirectory);
		} else {
			/*
			if (pm.items_class == null || pm.items_class.isEmpty()){
				throw new IonException("Не указан класс элементов коллекции у атрибута \""+cm.caption+"."+pm.caption+"\".");
			}
			try {
				getClass(pm.items_class, metaDirectory);
			} catch (IonException e){
				throw new IonException("Не найден класс элементов коллекции \""+pm.items_class+"\" у атрибута \""+cm.caption+"."+pm.caption+"\".");
			}
			*/
		}
		return null;
	}
	
	public File generateHbmFile(StoredClassMeta cm, File metaDirectory, File destination, String domain) throws IonException, IOException, ParserConfigurationException {
		loader.expandUserTypes(cm, new File(metaDirectory, "types"));
		if (!cm.is_struct){
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			try {
				//Set<String> types = new HashSet<String>();										
				Collection<Element> elements = new ArrayList<Element>();
				StoredClassMeta kh = cm;
				if (cm.ancestor != null && !cm.ancestor.isEmpty()){
					kh = getKeyHolder(cm.ancestor, metaDirectory);
				}
				
				Element id = null;
				
				for (StoredPropertyMeta pm : cm.properties){
					if (pm.name.equals(kh.key))
						id = pmToHb(pm, cm, kh, metaDirectory,doc);
					else {
						Element pel = pmToHb(pm, cm, kh, metaDirectory, doc);
						if (pel != null)
							elements.add(pel);
					}
				}

				DocumentType doctype = docBuilder.getDOMImplementation().createDocumentType("hibernate-mapping",
							 "-//Hibernate/Hibernate Mapping DTD//EN",
							 "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd");
				Element hmElement = doc.createElement("hibernate-mapping");
				hmElement.setAttribute("package", domain);
				doc.appendChild(hmElement);
				String tableName = dbSanitise(cm.name,"t");
                Element classEl;
                if (this.useDiscriminator)
                    classEl = getClassElementDiscr(doc, cm, kh, id, tableName, elements);
                else
                    classEl = getClassElement(doc, cm, kh, id, tableName, elements);
				classEl.setAttribute("name",cm.name);
				hmElement.appendChild(classEl);
					
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer;
				transformer = transformerFactory.newTransformer();
					
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
				DOMSource source = new DOMSource(doc);
				File mappingFile = new File(destination, domain.replace(".", File.separator)+"/"+cm.name+".hbm.xml");
				if(mappingFile.exists()) 
					mappingFile.delete();
				else
					mappingFile.getParentFile().mkdirs();
				
				StreamResult streamResult = new StreamResult(mappingFile);

				transformer.transform(source, streamResult);
				return mappingFile;
			} catch (TransformerConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace(System.err);
			} catch (TransformerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace(System.err);
			}
		}
		return null;
	}

	
    private Element getClassElement(Document xmlDoc, StoredClassMeta classMeta, 
            StoredClassMeta keyHolderMeta, Element idNode, String tableName, Collection<Element> properties)
            throws IonException {
        Element classEl;
        if (classMeta.ancestor == null || classMeta.ancestor.isEmpty()){
            classEl = xmlDoc.createElement("class");
            if (idNode != null)
                classEl.appendChild(idNode);
            else
                throw new IonException("Не указан ключевой атрибут класса "+classMeta.name);
        } else {
            classEl = xmlDoc.createElement("joined-subclass");
            classEl.setAttribute("extends", classMeta.ancestor);
            Element key = xmlDoc.createElement("key");
            key.setAttribute("column", dbSanitise(keyHolderMeta.key, "f"));
            classEl.appendChild(key);
        }
        classEl.setAttribute("table", tableName);
		for(Element e: properties){
			classEl.appendChild(e);
		}        
        return classEl;
    }
	
    private Element getClassElementDiscr(Document xmlDoc, StoredClassMeta classMeta, 
            StoredClassMeta keyHolderMeta, Element idNode, String tableName, Collection<Element> properties)
            throws IonException {
        Element classEl;
        if (classMeta.ancestor == null || classMeta.ancestor.isEmpty()){
        	classEl = xmlDoc.createElement("class");
        	if (idNode != null)
        		classEl.appendChild(idNode);
        	else
        		throw new IonException("Не указан ключевой атрибут класса "+classMeta.name);
            classEl.setAttribute("table", tableName);
            Element discriminator = xmlDoc.createElement("discriminator");
            discriminator.setAttribute("column", discriminatorColumnName);
            discriminator.setAttribute("length", Integer.toString(discriminatorLength));
            discriminator.setAttribute("type", "string");
            classEl.appendChild(discriminator);
    		for(Element e: properties){
    			classEl.appendChild(e);
    		}                    
        } else {
        	classEl = xmlDoc.createElement("subclass");
        	classEl.setAttribute("extends", classMeta.ancestor);
        	
    		for(Element e: properties){
    			classEl.appendChild(e);
    		}        
        	        	
        	Element joinedTable = xmlDoc.createElement("join");
        	classEl.appendChild(joinedTable);
            joinedTable.setAttribute("fetch", "select");
        	joinedTable.setAttribute("table", tableName);
        	
        	Element key = xmlDoc.createElement("key");
        	key.setAttribute("column", dbSanitise(keyHolderMeta.key, "f"));
        	joinedTable.appendChild(key);
        }
        classEl.setAttribute("discriminator-value", classMeta.name);
        return classEl;
    }
	
	public File generateHbmFile(String classname, File metaDirectory, File destination, String domain) throws IonException, IOException, ParserConfigurationException {
		StoredClassMeta cm = getClass(classname, metaDirectory);
		return generateHbmFile(cm, metaDirectory, destination, domain);
	}
	
	public File generateHbmFile(File meta, File metaDirectory, File destination, String domain) throws IonException, IOException, ParserConfigurationException {
		StoredClassMeta rc = loader.StoredClassMetaFromJson(meta, null);
		return generateHbmFile(rc, metaDirectory, destination, domain);
	}
	
	public File[] generateHbmFiles(File metaDirectory, File destination, String domain) throws IonException, ParserConfigurationException {
		File[] metaFiles = metaDirectory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".class.json");
			}
		});
		
		File[] result = new File[metaFiles.length];
		int i = 0;
		for (File meta: metaFiles){
			try {
				result[i] = generateHbmFile(meta, metaDirectory, destination, domain);
			} catch (IOException e) {
				throw new IonException(e);
			}
			i++;
		}
		return result;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}	
}
