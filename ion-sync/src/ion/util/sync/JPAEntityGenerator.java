package ion.util.sync;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.WordUtils;

public class JPAEntityGenerator {

	//public boolean useLazyLoading = false;

	public boolean useDiscriminator = false;

	public String discriminatorColumnName = "_type";

	public short discriminatorLength = 200;

	protected ModelLoader loader = new ModelLoader();

	protected boolean needGeneration(StoredPropertyMeta pm, StoredClassMeta cm) {
		return pm.autoassigned && pm.type == MetaPropertyType.INT.getValue();
	}

	private String setterName(String nm) {
		return "set" + WordUtils.capitalize(nm);
	}

	private String getterName(String nm) {
		return "get" + WordUtils.capitalize(nm);
	}

	protected String attrName(String nm) {
		return Character.isUpperCase(nm.charAt(1)) ? WordUtils.capitalize(nm)
				: WordUtils.uncapitalize(nm);
	}

	protected boolean isLob(StoredPropertyMeta pm) {
		if (pm.type == MetaPropertyType.STRUCT.getValue()
				|| pm.type == MetaPropertyType.HTML.getValue()
				|| pm.type == MetaPropertyType.TEXT.getValue())
			return true;
		return false;
	}

	private String formPropertyDef(StoredPropertyMeta pm, Object[] params) {
		return "private " + ((String) params[0]) + " " + attrName(pm.name)
				+ ";\n";
	}

	private String formSetter(StoredPropertyMeta pm, Object[] params) {
		return "public void " + setterName(pm.name) + "("
				+ ((String) params[0]) + " v){\n" + attrName(pm.name)
				+ " = v;\n}\n";
	}

	private String formGetter(StoredPropertyMeta pm, Object[] params) {
		return "public " + ((String) params[0]) + " " + getterName(pm.name)
				+ "(){\nreturn " + attrName(pm.name) + ";\n}\n";
	}

	protected String dbSanitise(String nm, String prefix) {
		return SyncUtils.dbSanitiseName(nm, prefix);
	}

	protected StoredClassMeta getClass(String classname, File metaDirectory)
			throws IonException {
		File metaFile = new File(metaDirectory, classname + ".class.json");
		StoredClassMeta rc = loader.StoredClassMetaFromJson(metaFile, null);
		return rc;
	}

	protected StoredClassMeta getKeyHolder(String classname, File metaDirectory)
			throws IonException {
		File metaFile = new File(metaDirectory, classname + ".class.json");
		StoredClassMeta rc = loader.StoredClassMetaFromJson(metaFile, new File(
				metaDirectory, "types"));
		if (rc.ancestor != null && !rc.ancestor.isEmpty())
			return getKeyHolder(rc.ancestor, metaDirectory);
		return rc;
	}

	protected Object[] parseType(StoredPropertyMeta pm, boolean in_struct,
			File metaDirectory) throws IonException {
		String t = "";
		Short p = 0;
		Short s = 0;
		String lib = "";
		Boolean is_string = false;
		Boolean is_int = false;
		Boolean is_auto = false;

		MetaPropertyType type = MetaPropertyType.fromInt(pm.type);
		switch (type) {
		case CUSTOM:
			throw new IonException("не развернут пользовательский тип");
		case BOOLEAN: {
			t = "Boolean";
		}
			break;
		case COLLECTION: {
			StoredClassMeta ic = getClass(pm.items_class, metaDirectory);
			if (ic.is_struct && !in_struct) {
				t = "String";
				is_string = true;
				p = 2000;
			} else {
					t = "List<" + pm.items_class + ">";
					lib = "java.util.List";
			}
		}
			break;
		case DATETIME: {
			t = "Date";
			lib = "java.util.Date";
		}
			break;
		case TEXT:
		case STRING:
		case HTML:
		case FILE:
		case IMAGE:
		case GUID:
		case PASSWORD:
		case USER:
		case URL: {
			t = "String";
			p = 200;
			if (type == MetaPropertyType.TEXT || type == MetaPropertyType.HTML)
				p = 2000;
			if (type == MetaPropertyType.FILE || type == MetaPropertyType.IMAGE
					|| type == MetaPropertyType.URL)
				p = 500;
			if (type == MetaPropertyType.GUID)
				p = 38;
			is_string = true;
		}
			break;
		case SET:
		case INT: {
			t = "Integer";
			p = 11;
			if (type == MetaPropertyType.SET)
				p = 3;
			if (type == MetaPropertyType.INT) {
				is_int = true;
				if (!pm.nullable)
					is_auto = true;
			}
		}
			break;
		case DECIMAL: {
			t = "BigDecimal";
			lib = "java.math.BigDecimal";
			p = 18;
			s = 2;
		}
			break;
		case REAL: {
			t = "Double";
			p = 18;
			s = 9;
		}
			break;
		case STRUCT: {
			if (in_struct)
				t = pm.ref_class;
			else {
				t = "String";
				is_string = true;
				p = 2000;
			}
		}
			break;
		case REFERENCE: {
			 StoredClassMeta rc = getClass(pm.ref_class, metaDirectory);
			 if (rc != null){
				 if (rc.is_struct)
					 t = "String";
				 else
					 t = pm.ref_class; 
			 } else throw new IonException("Неверно указан класс ссылочного атриубта!");
		}
			break;
		}
		if (t.isEmpty())
			return null;
		return new Object[] { t, p, s, lib, is_string, is_int, is_auto };
	}

	protected Object[] getPropertyType(StoredPropertyMeta pm,
			StoredClassMeta cm, File metaDirectory) throws IonException {
		Object[] result;
		try {
			result = parseType(pm, cm.is_struct, metaDirectory);
		} catch (IonException e) {
			throw new IonException("Не удалось определить тип атрибута \""
					+ cm.caption + "." + pm.caption + "\": " + e.getMessage());
		}

		if (result == null) {
			throw new IonException("Недопустимый тип у атрибута \""
					+ cm.caption + "." + pm.caption + "\".");
		}

		if (pm.size != null && pm.size > 0)
			result[1] = pm.size;

		if (pm.decimals != null && pm.decimals > 0)
			result[2] = pm.decimals;

		return result;
	}

	protected void generateImports(OutputStreamWriter w, StoredClassMeta cm,
																 File metaDirectory, boolean needGeration,
																 boolean needLob, boolean needRefs,
																 boolean needOneToMany, boolean needManyToMany)
																																							 throws IOException {
		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		boolean fetch_type = false;
		boolean join_column = false;
		w.write("import javax.persistence.Column;\n");
		w.write("import javax.persistence.Entity;\n");
		w.write("import javax.persistence.Table;\n");
		if (needLob)
			w.write("import javax.persistence.Lob;\n");
		if (needRefs) {
			w.write("import javax.persistence.ManyToOne;\n");
			fetch_type = true;
			join_column = true;
		}
		if (needOneToMany) {
			w.write("import javax.persistence.OneToMany;\n");
			fetch_type = true;
		}
		if (needManyToMany) {
			w.write("import javax.persistence.JoinTable;\n");
			w.write("import javax.persistence.ManyToMany;\n");
			fetch_type = true;
			join_column = true;
		}
		if(fetch_type)
			w.write("import javax.persistence.FetchType;\n");
		if(join_column)
			w.write("import javax.persistence.JoinColumn;\n");			
		if (isSuper) {
			if (needGeration) {
				w.write("import javax.persistence.GeneratedValue;\n");
				w.write("import javax.persistence.GenerationType;\n");
			}
			w.write("import javax.persistence.Inheritance;\n");
			w.write("import javax.persistence.InheritanceType;\n");
			w.write("import javax.persistence.Id;\n");
		}
		if (useDiscriminator) {
			if (isSuper) {
				w.write("import javax.persistence.DiscriminatorType;\n");
				w.write("import javax.persistence.DiscriminatorColumn;\n");
			}
			w.write("import javax.persistence.DiscriminatorValue;\n");
		}

		if (!isSuper) {
			w.write("import javax.persistence.PrimaryKeyJoinColumn;\n");
			w.write("import javax.persistence.SecondaryTable;\n");
		}
	}

	protected void generateClassMappingAnnotations(OutputStreamWriter w,
																								 StoredClassMeta cm,
																								 File metaDirectory)
																																		throws IOException,
																																		IonException {
		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		w.write("@Entity\n");
		
		if (useDiscriminator && !isSuper){
  			StoredClassMeta kh = getKeyHolder(cm.ancestor, metaDirectory);
  			w.write("@SecondaryTable(name=\"" + dbSanitise(cm.name, "t")
  					+ "\", pkJoinColumns = @PrimaryKeyJoinColumn(name=\""
  					+ dbSanitise(kh.key, "f") + "\"))\n");  			
		} else {
			w.write("@Table(name=\"" + dbSanitise(cm.name, "t") + "\")\n");			
		}
	}

	protected void generateClassInheritanceAnnotations(OutputStreamWriter w,
																										 StoredClassMeta cm,
																										 File metaDirectory)
																																				throws IOException,
																																				IonException {
		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		if (isSuper) {
			if (useDiscriminator) {
				w.write("@Inheritance(strategy=InheritanceType.SINGLE_TABLE)\n");
				w.write("@DiscriminatorColumn(name=\""
						+ discriminatorColumnName
						+ "\", discriminatorType=DiscriminatorType.STRING, length="
						+ discriminatorLength + ")\n");
			} else
				w.write("@Inheritance(strategy=InheritanceType.JOINED)\n");
		}
		if (useDiscriminator)
			w.write("@DiscriminatorValue(\"" + cm.name + "\")\n");
	}

	protected void generateClassAnnotations(OutputStreamWriter w,
																					StoredClassMeta cm, File metaDirectory)
																																								 throws IOException,
																																								 IonException {
		generateClassMappingAnnotations(w, cm, metaDirectory);
		generateClassInheritanceAnnotations(w, cm, metaDirectory);
	}

	protected void generateReferenceAnnotations(OutputStreamWriter w,
			StoredPropertyMeta pm, StoredClassMeta cm, File metaDirectory,
			Object[] params) throws IOException, IonException {
		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		w.write("@ManyToOne(fetch="
				+ (((pm.eager_loading != null) && pm.eager_loading) ? "FetchType.EAGER" : "FetchType.LAZY") + ")\n");
		w.write("@JoinColumn(name=\""+ dbSanitise(pm.name, "f") + "\"");
		if (useDiscriminator && !isSuper)
			w.write(", table=\"" + dbSanitise(cm.name, "t") + "\"");
		
		w.write(")\n"); 
	}

	protected void generateCollectionAnnotations(OutputStreamWriter w,
																							 StoredPropertyMeta pm,
																							 StoredClassMeta cm,
																							 File metaDirectory,
																							 Object[] params)
																															 throws IOException,
																															 IonException {
		StoredClassMeta items_class = getClass(pm.items_class, metaDirectory);
		if (items_class.is_struct) {
			w.write("@Lob\n");
			w.write("@Column(name=\"" + dbSanitise(pm.name, "f") + "\")\n");
		} else {
			if (pm.back_ref == null || pm.back_ref.isEmpty()) {
				String relationship_table_name = SyncUtils.RelationshipName(cm.name,pm.name);
				String cur_key = "master";
				String other_key = "detail";
				w.write("@ManyToMany(fetch="	+ (((pm.eager_loading != null) && pm.eager_loading) ? "FetchType.EAGER" : "FetchType.LAZY") + ")\n");
				w.write("@JoinTable(name=\""+relationship_table_name+"\",\n");
				w.write("joinColumns={@JoinColumn(name=\""+cur_key+"\")},\n");
				w.write("inverseJoinColumns={@JoinColumn(name=\""+other_key+"\")})\n");
			} else {
				w.write("@OneToMany(mappedBy=\"" + attrName(pm.back_ref) + "\", fetch="	+ (((pm.eager_loading != null) && pm.eager_loading) ? "FetchType.EAGER" : "FetchType.LAZY") + ")\n");
			}
		}

	}

	protected void generateClassMemberAnnotations(OutputStreamWriter w,
																								StoredPropertyMeta pm,
																								StoredClassMeta cm,
																								File metaDirectory,
																								Object[] params)
																																throws IOException,
																																IonException {
		if (cm.is_struct)
			return;
		
		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		if (pm.name.equals(cm.key)) {
			w.write("@Id\n");
			if (needGeneration(pm, cm))
				w.write("@GeneratedValue(strategy = GenerationType.AUTO)\n");
		}
		if (isLob(pm))
			w.write("@Lob\n");

		switch (MetaPropertyType.fromInt(pm.type)) {
		case REFERENCE:
			generateReferenceAnnotations(w, pm, cm, metaDirectory, params);
			break;
		case COLLECTION:
			generateCollectionAnnotations(w, pm, cm, metaDirectory, params);
			break;
		default:{
			w.write("@Column(name=\"" + dbSanitise(pm.name, "f") + "\"");
			if (useDiscriminator && !isSuper)
				w.write(", table=\"" + dbSanitise(cm.name, "t") + "\"");
			
			if ((Short) params[1] > 0) {
				w.write(", ");
				if (((Boolean) params[4])) {
					w.write("length=" + ((Short) params[1]).toString());
				} else {
					w.write("precision=" + ((Short) params[1]).toString());
					if ((Short) params[2] > 0) {
						w.write(", scale=" + ((Short) params[2]).toString());
					}
				}
			}
			w.write(")\n");			
		}
			break;
		}
	}

	public File generateSourceFile(StoredClassMeta cm, File metaDirectory,
			File destination, String domain) throws IonException, IOException {
		loader.expandUserTypes(cm, new File(metaDirectory, "types"));
		File result = new File(destination, cm.name + ".java");
		OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
				result), "utf-8");

		boolean isSuper = cm.ancestor == null || cm.ancestor.isEmpty();
		boolean need_lob = false;
		boolean need_generation = false;
		boolean has_references = false;
		boolean need_one_to_many = false;
		boolean need_many_to_many = false;
		Set<String> prop_types = new HashSet<String>();
		HashMap<StoredPropertyMeta, Object[]> props = new HashMap<StoredPropertyMeta, Object[]>();

		for (StoredPropertyMeta pm : cm.properties) {
			if (pm.type == null) {
				w.close();
				throw new IonException("Не указан тип атрибута \"" + cm.caption + "."
						+ pm.caption + "\".");
			}
			
			StoredClassMeta items_class = null;
			if (pm.type.equals(MetaPropertyType.COLLECTION.getValue())) {
				if (pm.items_class == null || pm.items_class.isEmpty()) {
					w.close();
					throw new IonException("Не указан класс элементов коллекции у атрибута \""
							+ cm.caption + "." + pm.caption + "\".");
				}
				try {
					items_class = getClass(pm.items_class, metaDirectory);
				} catch (IonException e) {
					w.close();
					throw new IonException("Не найден класс элементов коллекции \""
							+ pm.items_class + "\" у атрибута \"" + cm.caption + "."
							+ pm.caption + "\".");
				}
			}
			
			Object[] prop_type = null;
			try {
				prop_type = getPropertyType(pm, cm, metaDirectory);
			} catch (IonException e) {
				w.close();
				throw e;
			}
			if (prop_type != null) {
				if (needGeneration(pm, cm))
					need_generation = true;

				if (isLob(pm))
					need_lob = true;

				if (pm.type.equals(MetaPropertyType.REFERENCE.getValue()))
					has_references = true;

				if (pm.type.equals(MetaPropertyType.COLLECTION.getValue())) {
					if (items_class.is_struct) {
						if (!cm.is_struct){
							need_lob = true;
						}
					}
					
					if (pm.back_ref != null && !pm.back_ref.isEmpty())
						need_one_to_many = true;
					else
						need_many_to_many = true;
					
					if (!has_references)
						has_references = need_one_to_many || need_many_to_many;
				}

				if ((prop_type != null) && (((String) prop_type[3]).length() > 0))
					prop_types.add((String) prop_type[3]);

				props.put(pm, prop_type);
			}
		}

		try {
			w.write("package " + domain + ";\n");

			if (!cm.is_struct)
				generateImports(w, cm, metaDirectory, need_generation, need_lob,
						has_references, need_one_to_many, need_many_to_many);

			for (String imp : prop_types) {
				w.write("import " + imp + ";\n");
			}

			w.write("\n");

			if (!cm.is_struct)
				generateClassAnnotations(w, cm, metaDirectory);

			w.write("public class " + cm.name
					+ ((!isSuper) ? " extends " + cm.ancestor : "") + " {\n");
			w.write("\n");

			for (Entry<StoredPropertyMeta, Object[]> i : props.entrySet()) {
				StoredPropertyMeta pm = i.getKey();
				Object[] params = i.getValue();
				generateClassMemberAnnotations(w, pm, cm, metaDirectory, params);
				w.write(formPropertyDef(pm, params));
				w.write("\n");
			}

			for (Entry<StoredPropertyMeta, Object[]> i : props.entrySet()) {
				StoredPropertyMeta pm = i.getKey();
				Object[] params = i.getValue();
				w.write(formSetter(pm, params));
				w.write("\n");
			}

			for (Entry<StoredPropertyMeta, Object[]> i : props.entrySet()) {
				StoredPropertyMeta pm = i.getKey();
				Object[] params = i.getValue();
				w.write(formGetter(pm, params));
				w.write("\n");
			}

			w.write("}\n");

			w.flush();
			w.close();
		} catch (IonException e) {
			w.close();
			throw e;
		} catch (IOException e) {
			w.close();
			throw e;
		} finally {
			w.close();
		}

		return result;
	}

	public File generateSourceFile(String classname, File metaDirectory,
			File destination, String domain) throws IonException, IOException {
		StoredClassMeta cm = getClass(classname, metaDirectory);
		return generateSourceFile(cm, metaDirectory, destination, domain);
	}

	public File generateSourceFile(File meta, File metaDirectory,
			File destination, String domain) throws IonException, IOException {
		StoredClassMeta rc = loader.StoredClassMetaFromJson(meta, null);
		return generateSourceFile(rc, metaDirectory, destination, domain);
	}

	public File[] generateSources(File metaDirectory, File destination,
			String domain) throws IonException {
		File[] metaFiles = metaDirectory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".class.json");
			}
		});

		File[] result = new File[metaFiles.length];
		int i = 0;
		for (File meta : metaFiles) {
			try {
				result[i] = generateSourceFile(meta, metaDirectory,
						destination, domain);
			} catch (IOException e) {
				throw new IonException(e);
			}
			i++;
		}
		return result;
	}

}
