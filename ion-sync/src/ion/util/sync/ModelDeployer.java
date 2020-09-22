package ion.util.sync;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
//import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.LinkedList;

import org.apache.commons.lang.StringEscapeUtils;

import liquibase.Liquibase;
import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RawSQLChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.core.ForeignKeyConstraintType;

public class ModelDeployer {

	String metaDirectory;

	String dbPatchDirectory;

	String tablePrefix = "t";

	String columnPrefix = "f";

	String foreignKeyPrefix = "fk";

	private String structSeparator = "$";

	private int defaultStringLength = 200;

	boolean referentialIntegrity = true;

	private boolean useDiscriminator = false;

	private String discriminatorColumnName = "_type";

	private short discriminatorLength = 200;

	public String getStructSeparator() {
		return structSeparator;
	}

	public void setStructSeparator(String structSeparator) {
		this.structSeparator = structSeparator;
	}

	public boolean getUseDiscriminator() {
		return this.useDiscriminator;
	}

	public void setUseDiscriminator(boolean value) {
		this.useDiscriminator = value;
	}

	public String getDiscriminatorColumnName() {
		return this.discriminatorColumnName;
	}

	public void setDiscriminatorColumnName(String value) {
		this.discriminatorColumnName = value;
	}

	public short getDiscriminatorLength() {
		return this.discriminatorLength;
	}

	public void setDiscriminatorLength(short value) {
		this.discriminatorLength = value;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public String getColumnPrefix() {
		return columnPrefix;
	}

	public void setColumnPrefix(String columnPrefix) {
		this.columnPrefix = columnPrefix;
	}

	public String getFKPrefix() {
		return foreignKeyPrefix;
	}

	public void setFKPrefix(String foreignKeyPrefix) {
		this.foreignKeyPrefix = foreignKeyPrefix;
	}

	public int getDefaultStringLength() {
		return defaultStringLength;
	}

	public void setDefaultStringLength(int defaultStringLength) {
		this.defaultStringLength = defaultStringLength;
	}

	String author = "model-deployer";

	// Platform platform;

	// Database destination;

	private class ClassDefRecord {
		public StoredClassMeta meta;

		public Set<String> Refs;

		public Map<String, StoredPropertyMeta> properties;

		private Map<String, ClassDefRecord> _classes;

		private ClassDefRecord _ancestor;

		private boolean _ancestorChecked = false;

		public ClassDefRecord(StoredClassMeta meta,
													Map<String, ClassDefRecord> classes) {
			this.meta = meta;
			_classes = classes;
			properties = new HashMap<String, StoredPropertyMeta>();
			for (StoredPropertyMeta pm : meta.properties) {
				if (pm.type != MetaPropertyType.STRUCT.getValue())
					properties.put(pm.name, pm);
			}
			Refs = new HashSet<String>();
		}

		public ClassDefRecord getAncestor() {
			if (_ancestor == null && !_ancestorChecked) {
				if (meta.ancestor != null && meta.ancestor.length() > 0)
					_ancestor = _classes.get(meta.ancestor);
				_ancestorChecked = true;
			}
			return _ancestor;
		}

		public StoredPropertyMeta getKeyProperty() {
			if (getAncestor() == null)
				return properties.get(meta.key);
			return getAncestor().getKeyProperty();
		}

		public boolean IsKey(String name) {
			if (getAncestor() == null) {
				return meta.key.equals(name);
			}
			return getAncestor().IsKey(name);
		}
	}

	public ModelDeployer(String author, String metaDirectory,
											 String dbPatchDirectory, boolean ri,
											 ServiceLocator locator, boolean useDiscriminator) {
		this(author, metaDirectory, dbPatchDirectory, ri, locator);
		this.useDiscriminator = useDiscriminator;
	}

	public ModelDeployer(String author, String metaDirectory,
											 String dbPatchDirectory, boolean ri,
											 ServiceLocator locator) {
		this.metaDirectory = metaDirectory;
		this.dbPatchDirectory = dbPatchDirectory;
		this.author = author;
		this.referentialIntegrity = ri;
		if (locator != null)
			ServiceLocator.setInstance(locator);
	}

	public ModelDeployer(String author, String metaDirectory,
											 String dbPatchDirectory, ServiceLocator locator) {
		this(author, metaDirectory, dbPatchDirectory, true, locator);
	}

	public ModelDeployer(String author, String metaDirectory,
											 String dbPatchDirectory, boolean ri) {
		this(author, metaDirectory, dbPatchDirectory, ri, null);
	}

	public ModelDeployer(String author, String metaDirectory,
											 String dbPatchDirectory) {
		this(author, metaDirectory, dbPatchDirectory, true);
	}

	private ClassDefRecord[] getOrderedMetas(Map<String, ClassDefRecord> definitions)
																																									 throws IonException {
		Map<String, StoredClassMeta> classes = new HashMap<String, StoredClassMeta>();
		Map<String, StoredClassMeta> structs = new HashMap<String, StoredClassMeta>();
		File f = new File(metaDirectory);
		if (f.exists()) {

			File[] metafiles = f.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".class.json");
				}
			});

			ModelLoader loader = new ModelLoader();
			StoredClassMeta c;
			try {
				for (File cf : metafiles)
					if (!classes.containsKey(cf.getName().replace(".class.json", ""))
							&& !structs.containsKey(cf.getName().replace(".class.json", ""))) {
						c = loader.StoredClassMetaFromJson(cf, new File(f, "types"));
						if (!c.is_struct) {
							classes.put(c.name, c);
							definitions.put(c.name, new ClassDefRecord(c, definitions));
						} else
							structs.put(c.name, c);
					}
			} catch (Exception e) {
				throw new IonException(e);
			}
		} else
			throw new IonException("Meta directory not set!");

		for (StoredClassMeta cm : classes.values()) {
			if (cm.ancestor != null && cm.ancestor.length() > 0)
				definitions.get(cm.name).Refs.add(cm.ancestor);
			for (StoredPropertyMeta pm : cm.properties) {
				if (pm.type == MetaPropertyType.REFERENCE.getValue()) {
					if (referentialIntegrity && pm.ref_class != null && pm.ref_class.length() > 0)
						definitions.get(cm.name).Refs.add(pm.ref_class);
				} else if (pm.type == MetaPropertyType.STRUCT.getValue()) {
					if (pm.ref_class != null && pm.ref_class.length() > 0
							&& structs.containsKey(pm.ref_class)){
						StoredClassMeta struct = structs.get(pm.ref_class);
						while(struct != null){
    					for (StoredPropertyMeta spm : struct.properties) {
    						StoredPropertyMeta struct_attr = new StoredPropertyMeta(spm.order_number,
    																					pm.name
    																					+ this.structSeparator
    																					+ spm.name,
    																					spm.caption,
    																					spm.type,
    																					spm.size,
    																					spm.decimals,
    																					spm.nullable,
    																					spm.readonly,
    																					spm.indexed,
    																					spm.unique,
    																					spm.autoassigned,
    																					spm.hint,
    																					spm.default_value,
    																					spm.ref_class,
    																					spm.items_class,
    																					spm.back_ref,
    																					spm.back_coll,
    																					spm.binding,
    																					spm.sel_conditions,
    																					spm.sel_sorting,
    																					spm.selection_provider,
    																					spm.index_search,
    																					spm.eager_loading,
    																					spm.semantic,
    																					spm.formula);
    						definitions.get(cm.name).properties.put(struct_attr.name, struct_attr);
    					}
    					struct = structs.get(struct.ancestor);
						}
					}
				}
			}
		}

		ClassDefRecord[] classlist = definitions.values().toArray(new ClassDefRecord[definitions.values().size()]);
		
	/*
	 XXX сортировка по идее вообще не нужна - внешние ключи создаются после создания таблиц
		Arrays.sort(classlist, new Comparator<ClassDefRecord>() {
			public int compare(ClassDefRecord o1, ClassDefRecord o2) {
				if (o1.Refs.contains(o2.meta.name))
					return 1;
				if (o2.Refs.contains(o1.meta.name))
					return -1;
				return 0;
			}
		});
	*/
		return classlist;
	}

	protected ChangeSet createChangeSet(String changeLogId, int index,
																			String dbms, DatabaseChangeLog changeLog) {
		ChangeSet result = new ChangeSet(changeLogId + "-" + String.valueOf(index),
												 author,
												 false,
												 false,
												 changeLog.getFilePath(),
												 "",
												 dbms,
												 true,
												 ObjectQuotingStrategy.QUOTE_ALL_OBJECTS,
												 changeLog);
		result.setFailOnError(false);
		return result;
	}

	protected boolean unsizedType(int type) {
		switch (type) {
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
			case Types.BIT:
			case Types.DATE:
			case Types.BLOB:
			case Types.NCLOB:
			case Types.CLOB:
				return true;
			default:
				return false;
		}
	}

	// есть общая длина
	protected boolean isPrecisionedType(int type) {
		switch (type) {
			case Types.NUMERIC:
				// case Types.BIGINT:
				// case Types.INTEGER:
				// case Types.SMALLINT:
				// case Types.TINYINT:
				// case Types.DOUBLE:
			case Types.DECIMAL:
				// case Types.FLOAT:
				// case Types.REAL:
			case Types.VARCHAR:
			case Types.CHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
				return true;
			default:
				return false;
		}
	}

	// есть дробная часть
	protected boolean isScaledType(int type) {
		switch (type) {
		case Types.NUMERIC:
		//case Types.DOUBLE:
		case Types.DECIMAL:
		//case Types.FLOAT:
		//case Types.REAL:
			return true;
		default:
			return false;
		}
	}

	protected int getPropertyJdbcType(StoredPropertyMeta pm) {
		switch (MetaPropertyType.fromInt(pm.type)) {
			case BOOLEAN:
				return Types.BIT;
			case DATETIME:
				return Types.DATE;
			case DECIMAL:
				return Types.NUMERIC;
			case INT:
				return (pm.size > 11) ? Types.BIGINT : Types.INTEGER;
			case SET:
				return Types.SMALLINT;
			case REAL:
				return Types.DOUBLE;
			case GUID:
			case FILE:
			case IMAGE:
			case PASSWORD:
			case STRING:
			case URL:
				return Types.VARCHAR;
			case HTML:
			case TEXT:
				return Types.LONGVARCHAR;
			default:
				return Types.CHAR;
		}
	}

	protected boolean cmpPropertyJdbcType(StoredPropertyMeta pm, int type) {
		switch (MetaPropertyType.fromInt(pm.type)) {
			case BOOLEAN:
				return type == Types.BIT;
			case DATETIME:
				return (type == Types.DATE) || (type == Types.TIMESTAMP) || (type == Types.TIMESTAMP_WITH_TIMEZONE);
			case DECIMAL:
				return type == Types.NUMERIC || type == Types.DECIMAL;
			case INT:
				return ((pm.size == null) || (pm.size > 11)) ? (type == Types.BIGINT)
																										: (type == Types.INTEGER);
			case SET:
				return type == Types.SMALLINT;
			case REAL:
				return type == Types.DOUBLE || type == Types.REAL
						|| type == Types.FLOAT;
			case GUID:
			case FILE:
			case IMAGE:
			case PASSWORD:
			case STRING:
			case URL:
				return type == Types.VARCHAR || type == Types.NVARCHAR
						|| type == Types.CHAR || type == Types.NCHAR;
			case HTML:
			case TEXT:
				return type == Types.CLOB || type == Types.LONGVARCHAR
						|| type == Types.LONGNVARCHAR || type == Types.NCLOB
						|| type == Types.LONGVARBINARY;
			default:
				return false;
		}
	}

	protected String getColumnType(StoredPropertyMeta pm) {
		return getColumnType(pm, false);
	}

	protected String getCastType(StoredPropertyMeta pm) {
		switch (MetaPropertyType.fromInt(pm.type)) {
			case DATETIME:
				return /* (pm.size != null && pm.size > 2)?"datetime": */"datetime";
			case DECIMAL:
				return "decimal";
			case BOOLEAN:
			case INT:
			case SET:
				return "int";
			case REAL:
				return "double";
			case GUID:
			case FILE:
			case IMAGE:
			case PASSWORD:
			case STRING:
			case URL:
				return "char";
			case HTML:
			case TEXT:
				return "char";
			default:
				return "char";
		}
	}

	protected Short getColumnSize(StoredPropertyMeta pm) {
		if (isPrecisionedType(pm.type)) {
			switch (MetaPropertyType.fromInt(pm.type)) {
				case GUID:
					return 36;
				case URL:
				case FILE:
				case IMAGE:
					return (pm.size == null || pm.size == 0) ? (short) 2000
																										: pm.size;
				case PASSWORD:
				case STRING:
					return (pm.size == null || pm.size == 0) ? (short) defaultStringLength
																									: pm.size;
				default:
					return (pm.size == null || pm.size == 0) ? null : pm.size;
			}
		}
		return null;
	}

	protected String getColumnType(StoredPropertyMeta pm, boolean trim_size) {
		Short ss = getColumnSize(pm);
		String s = (ss == null || ss == 0) ? "" : ss.toString();
		if (s.isEmpty())
			trim_size = true;
		String d = (pm.decimals == null) ? "0" : pm.decimals.toString();
		switch (MetaPropertyType.fromInt(pm.type)) {
		case BOOLEAN:
			return "boolean";
		case DATETIME:
			return /* (pm.size != null && pm.size > 2)?"datetime": */"datetime";
		case DECIMAL:
			return "number" + (trim_size ? "" : ("(" + s + "," + d + ")"));
		case INT:
			return ((pm.size == null || pm.size > 11) ? "bigint" : "integer")
					+ (trim_size ? "" : ("(" + s + ")"));
		case SET:
			return "smallint"/* + (trim_size ? "" : ("(" + s + ")"))*/;
		case REAL:
			return "double"/* + (trim_size ? "" : ("(" + s + "," + d + ")"))*/;
		case GUID:
			return "char(36)";
		case URL:
		case FILE:
		case IMAGE:return "varchar"
				+ (trim_size ? "(2000)"
											: ("(" + s + ")"));
		case PASSWORD:
		case STRING:
			return "varchar"
					+ (trim_size ? "(" + defaultStringLength + ")"
							: ("(" + s + ")"));
		case HTML:
		case TEXT:
			return "text";
		default:
			return "char" + (trim_size ? "" : ("(" + s + ")"));
		}
	}

	protected StoredPropertyMeta deployedPM(StoredPropertyMeta pm,
																					Map<String, ClassDefRecord> classes) {
		if (pm.type == MetaPropertyType.REFERENCE.getValue()) {
			ClassDefRecord c = classes.get(pm.ref_class);
			if (c != null)
				return c.getKeyProperty();
		}
		return pm;
	}

	/*
	 * protected String classKeyName(ClassDefRecord cdr) { if (cdr.getAncestor()
	 * != null) return classKeyName(cdr.getAncestor()); return cdr.meta.key; }
	 */

	protected AddForeignKeyConstraintChange addFK(ClassDefRecord bc,
																								StoredPropertyMeta bpm,
																								ClassDefRecord rc,
																								boolean inheritance)
																																		throws Exception {
		if (referentialIntegrity || inheritance) {
			if (rc == null)
				throw new Exception("Внешний ключ не может быть создан. Для ссылочного атрибута \""
						+ bc.meta.caption + "." + bpm.caption + "\" не указан класс ссылки!");
			
			AddForeignKeyConstraintChange fk = new AddForeignKeyConstraintChange();
			fk.setConstraintName(dbName(bc.meta.name.toLowerCase() + "_"
																			+ bpm.name.toLowerCase(),
																	foreignKeyPrefix));
			fk.setBaseTableName(dbName(bc.meta.name, tablePrefix));
			fk.setBaseColumnNames(dbName(bpm.name, columnPrefix));
			fk.setReferencedTableName(dbName(rc.meta.name, tablePrefix));
			fk.setReferencedColumnNames(dbName(rc.getKeyProperty().name, columnPrefix));
			if (inheritance)
				fk.setDeleteCascade(true);
			else
				fk.setOnDelete(bpm.nullable ? ForeignKeyConstraintType.importedKeySetNull
																	 : ForeignKeyConstraintType.importedKeyRestrict);

			return fk;
		}
		return null;
	}

	protected void columnSetup(ColumnConfig column, StoredPropertyMeta pm,
														 StoredPropertyMeta deployed, ClassDefRecord cm,
														 Map<String, ClassDefRecord> classes,
														 Collection<AddForeignKeyConstraintChange> createFK)
																																								throws Exception {

		column.setName(dbName(pm.name, columnPrefix));
		column.setType(getColumnType(deployed));

		ConstraintsConfig constraints = new ConstraintsConfig();

		boolean nullable = true;
		boolean auto_increment = false;

		if (cm.IsKey(pm.name)) {
			constraints.setPrimaryKey(true);
		} else {
			if (pm.unique)
				constraints.setUnique(true);
			if (!pm.nullable) {
				nullable = false;
				constraints.setNullable(false);
			}
		}

		/*
		if (cm.IsKey(pm.name) && cm.getAncestor() == null) {
			if (deployed.type.equals(MetaPropertyType.INT.getValue())
					&& deployed.nullable) {
				auto_increment = true;
				column.setAutoIncrement(true);
			}
		}
		 */
		if (pm.autoassigned && pm.type == MetaPropertyType.INT.getValue()) {
			auto_increment = true;
			column.setAutoIncrement(true);
		}

		String dv = pm.default_value;

		if (!auto_increment && dv != null) {
			if (!dv.isEmpty() || !nullable) {
				switch (MetaPropertyType.fromInt(pm.type)) {
					case DATETIME: {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date d = null;
						try {
							d = format.parse(dv);
						} catch (ParseException ex) {
						}
						if (d == null)
							dv = null;
						else
							dv = format.format(d);
					}
						break;
					case BOOLEAN:
						dv = String.valueOf(Boolean.parseBoolean(dv));
						break;
					case REAL:
					case DECIMAL:
						dv = dv.isEmpty() ? "0" : String.valueOf(Float.parseFloat(dv));
						break;
					case INT:
					case REFERENCE:
					case SET:
						dv = dv.isEmpty() ? "0" : String.valueOf(Integer.parseInt(dv));
						break;
					default:
						break;
				}
				column.setDefaultValue(dv);
			}
		}

		if (pm.type == MetaPropertyType.REFERENCE.getValue()) {
			AddForeignKeyConstraintChange ch = addFK(cm, pm,
																							 classes.get(pm.ref_class), false);
			if (ch != null)
				createFK.add(ch);
		} else if (cm.IsKey(pm.name) && !cm.properties.containsKey(pm.name)) {
			if (cm.getAncestor() != null)
				createFK.add(addFK(cm, pm, cm.getAncestor(), true));
		}
		column.setConstraints(constraints);
	}

	protected int processIndexed(StoredPropertyMeta pm, ClassDefRecord cm,
															 String changeLogId, int index,
															 DatabaseChangeLog changeLog,
															 Collection<ChangeSet> changeSets) {
		if (pm.indexed && !pm.unique) {
			CreateIndexChange ci_change = new CreateIndexChange();
			ci_change.setTableName(dbName(cm.meta.name, tablePrefix));
			ci_change.setIndexName("ix_" + cm.meta.name.toLowerCase() + "_"
					+ pm.name.toLowerCase());
			AddColumnConfig icol = new AddColumnConfig();
			icol.setName(dbName(pm.name, columnPrefix));
			ci_change.addColumn(icol);
			ChangeSet changeSet = createChangeSet(changeLogId, index, null, changeLog);
			changeSet.addChange(ci_change);
			changeSets.add(changeSet);
			return index + 1;
		}
		return index;
	}

	protected String castExpression(StoredPropertyMeta pm,
																	StoredPropertyMeta deployed,
																	String fieldname, int oldType, int oldSize,
																	int oldDecimals) {
		String expr = fieldname;
		if (!cmpPropertyJdbcType(pm, oldType))
			expr = "cast(" + expr + " as " + getCastType(deployed) + ")";
		/*
		 * switch (oldType){ case Types.DOUBLE: case Types.NUMERIC:if (pm.size !=
		 * oldSize || pm.decimals != oldDecimals) expr =
		 * "SUBSTRING ("+fieldname+" FROM 1 FOR "+pm.size.toString()+")";break; case
		 * Types.BIGINT: case Types.INTEGER: case Types.SMALLINT:if (pm.size !=
		 * oldSize) expr =
		 * "SUBSTRING ("+fieldname+" FROM 1 FOR "+pm.size.toString()+")";break; case
		 * Types.CHAR: case Types.VARCHAR: case Types.LONGVARCHAR:if (pm.size !=
		 * oldSize) expr =
		 * "SUBSTRING ("+fieldname+" FROM 1 FOR "+pm.size.toString()+")";break; }
		 */
		return expr;
	}

	private String castDoubleLevelMigrationExpression(String curColumn,
																										String oldColumn,
																										String fromTable,
																										String curFromColumn,
																										String oldFromColumn) {
		return curColumn + "=" + "(select " + curFromColumn + " from " + fromTable
				+ " where " + oldFromColumn + "=" + oldColumn + ")";
	}

	protected String dbName(String name, String prefix) {
		return SyncUtils.dbSanitiseName(name, prefix);
	}

	private String columnType(ResultSet crs, boolean precisioned, boolean scaled) throws SQLException {
		return crs.getString("TYPE_NAME").toLowerCase()
		+ ((precisioned && (crs.getInt("COLUMN_SIZE") > 0) && (crs.getInt("COLUMN_SIZE") < 65535))?
			("(" + String.valueOf(crs.getInt("COLUMN_SIZE")) + ((scaled && (crs.getInt("DECIMAL_DIGITS") > 0)) ? ", "
				+ String.valueOf(crs.getInt("DECIMAL_DIGITS")): "") + ")"):"");
	}

	
/*
 * TODO Сделать что-то с злоебучей копипастой в этом злоебучем методе	
 */
	
	protected int establishRelationship(ClassDefRecord currentClass,
																			StoredPropertyMeta colpm,
																			ClassDefRecord otherClass,
																			String changeLogId, int counter,
																			DatabaseChangeLog changeLog,
																			Map<String, ChangeSet> chsCreateRelTable,
																			Map<String, ChangeSet> chsCreateRelPK,
																			Map<String, ChangeSet> chsDropRelPK,
																			List<ChangeSet> chsRenameRelColumns,
																			List<ChangeSet> chsModifyRelColumnType,
																			List<ChangeSet> chsAddRelColumns,
																			List<ChangeSet> chsRelDataMigrate,
																			Map<String, ChangeSet> chsDropFK,
																			List<ChangeSet> chsDropIndeces,
																			List<ChangeSet> chsCreateRelFK,
																			DatabaseMetaData existing_meta)
																																		 throws Exception {
		if (currentClass == null || otherClass == null)
			return counter;
		
		String relationshipName = SyncUtils.RelationshipName(currentClass.meta.name,
																												 colpm.name);
		HashMap<String, ClassDefRecord> classes = new HashMap<String, ClassDefRecord>();
		String curKeyName = "master";
		String otherKeyName = "detail";
		classes.put(curKeyName, currentClass);
		classes.put(otherKeyName, otherClass);
		ChangeSet changeSet;

		ResultSet table = existing_meta.getTables(null, null, relationshipName,
																							null);
		if (table.next()) {
			boolean need_reset_PK = false;

			AddColumnChange addColumns = new AddColumnChange();
			addColumns.setTableName(relationshipName);

			String migration = "";

			for (Entry<String, ClassDefRecord> relClass : classes.entrySet()) {
				StoredPropertyMeta pm = relClass.getValue().getKeyProperty();
				if (pm == null)
					return counter;
				ResultSet crs = existing_meta.getColumns(null, null, relationshipName,
																								 relClass.getKey());
				if (crs.next()) {
					boolean need_rename = false;
					boolean need_resize = false;

					if (!cmpPropertyJdbcType(pm, crs.getInt("DATA_TYPE")))
						need_rename = true;
					else if ((pm.size != null)
							&& isPrecisionedType(crs.getInt("DATA_TYPE"))
							&& crs.getInt("COLUMN_SIZE") > pm.size)
						need_rename = true;
					else if ((pm.size != null) && (pm.decimals != null)
							&& isScaledType(crs.getInt("DATA_TYPE"))
							&& crs.getInt("COLUMN_SIZE") == pm.size
							&& crs.getInt("DECIMAL_DIGITS") > pm.decimals)
						need_rename = true;
					if (!need_rename) {
						if (pm.type == MetaPropertyType.DATETIME.getValue() && (crs.getInt("DATA_TYPE") == Types.DATE))
							need_resize = true;
						else if ((pm.size != null) && isPrecisionedType(crs.getInt("DATA_TYPE"))
								&& crs.getInt("COLUMN_SIZE") < pm.size)
							need_resize = true;
						else if ((pm.size != null) && (pm.decimals != null)
								&& isScaledType(crs.getInt("DATA_TYPE"))
								&& crs.getInt("COLUMN_SIZE") == pm.size
								&& crs.getInt("DECIMAL_DIGITS") < pm.decimals)
							need_resize = true;
					}
					if (need_rename) {
						RenameColumnChange renameColumn = new RenameColumnChange();
						renameColumn.setTableName(relationshipName);
						renameColumn.setOldColumnName(relClass.getKey());
						renameColumn.setNewColumnName(relClass.getKey() + "_" + changeLogId);
						renameColumn
								.setColumnDataType(columnType(crs,isPrecisionedType(crs.getInt("DATA_TYPE")),isScaledType(crs.getInt("DATA_TYPE"))));
						changeSet = createChangeSet(changeLogId, counter, null,
								changeLog);
						changeSet.addChange(renameColumn);
						chsRenameRelColumns.add(changeSet);
						counter++;

						need_reset_PK = true;

						AddColumnConfig addColumn = new AddColumnConfig();
						addColumn.setName(relClass.getKey());
						addColumn.setType(getColumnType(pm));
						addColumn.setConstraints(new ConstraintsConfig().setNullable(false));
						addColumns.addColumn(addColumn);

						// XXX:Замудренная миграция данных
						ResultSet fk = existing_meta.getImportedKeys(null, null,
																												 relationshipName);
						while (fk.next()) {
							if (fk.getString("FKCOLUMN_NAME").equals(relClass.getKey())) {
								if (fk.getString("PKTABLE_NAME")
											.equals(dbName(relClass.getValue().meta.name, tablePrefix))) {
									migration = migration
											+ (migration.isEmpty() ? "" : ",\n")
											+ castDoubleLevelMigrationExpression(relClass.getKey(),
																													 relClass.getKey()
																															 + "_"
																															 + changeLogId,
																													 dbName(relClass.getValue().meta.name,
																																	tablePrefix),
																													 dbName(pm.name,
																																	columnPrefix),
																													 fk.getString("PKCOLUMN_NAME")
																															 + "_"
																															 + changeLogId);
								}
								break;
							}
						}

					} else if (need_resize) {
						ModifyDataTypeChange modifyType = new ModifyDataTypeChange();
						modifyType.setTableName(relationshipName);
						modifyType.setColumnName(relClass.getKey());
						modifyType.setNewDataType(getColumnType(pm));
						changeSet = createChangeSet(changeLogId, counter, null, changeLog);
						changeSet.addChange(modifyType);
						chsModifyRelColumnType.add(changeSet);
						counter++;
					}
				}
			}

			if (addColumns.getColumns().size() > 0) {
				changeSet = createChangeSet(changeLogId, counter, null, changeLog);
				changeSet.addChange(addColumns);
				chsAddRelColumns.add(changeSet);
				counter++;
			}

			if (need_reset_PK) {
				if (!chsDropRelPK.containsKey(relationshipName)) {
					ResultSet FKs = existing_meta.getImportedKeys(null, null,
																												relationshipName);
					while (FKs.next()) {
						if (!chsDropFK.containsKey(FKs.getString("FK_NAME"))) {
							DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
							dropFK.setBaseTableName(relationshipName);
							dropFK.setConstraintName(FKs.getString("FK_NAME"));
							changeSet = createChangeSet(changeLogId, counter, null, changeLog);
							changeSet.addChange(dropFK);
							chsDropFK.put(FKs.getString("FK_NAME"), changeSet);
							counter++;

							ResultSet irs = existing_meta.getIndexInfo(null, null,
																												 relationshipName,
																												 false, true);
							while (irs.next()) {
								if (irs.getString("INDEX_NAME")
											 .equals(FKs.getString("FK_NAME"))) {
									DropIndexChange dropIndex = new DropIndexChange();
									dropIndex.setTableName(relationshipName);
									dropIndex.setIndexName(FKs.getString("FK_NAME"));
									changeSet = createChangeSet(changeLogId, counter, null,
																							changeLog);
									changeSet.addChange(dropIndex);
									changeSet.setFailOnError(false);
									chsDropIndeces.add(changeSet);
									counter++;
								}
							}
						}
					}

					DropPrimaryKeyChange dropPK = new DropPrimaryKeyChange();
					dropPK.setTableName(relationshipName);
					changeSet = createChangeSet(changeLogId, counter, null, changeLog);
					changeSet.addChange(dropPK);
					chsDropRelPK.put(relationshipName, changeSet);
					counter++;
				}
				//if (referentialIntegrity) {
					if (!chsCreateRelPK.containsKey(relationshipName)) {
						int i = 0;
						for (Entry<String, ClassDefRecord> relClass : classes.entrySet()) {
							AddForeignKeyConstraintChange addFK = new AddForeignKeyConstraintChange();
							addFK.setConstraintName("fk_"+relationshipName
									+ "_" + i);
							addFK.setBaseTableName(relationshipName);
							addFK.setBaseColumnNames(relClass.getKey());
							addFK.setReferencedTableName(dbName(relClass.getValue().meta.name,
																									tablePrefix));
							addFK.setReferencedColumnNames(dbName(relClass.getValue()
																														.getKeyProperty().name,
																										columnPrefix));
							addFK.setDeleteCascade(true);
							changeSet = createChangeSet(changeLogId, counter, null, changeLog);
							changeSet.addChange(addFK);
							chsCreateRelFK.add(changeSet);
							i++;
							counter++;
						}
					}
					AddPrimaryKeyChange addPK = new AddPrimaryKeyChange();
					addPK.setTableName(relationshipName);
					addPK.setColumnNames(curKeyName + "," + otherKeyName);
					changeSet = createChangeSet(changeLogId, counter, null, changeLog);
					changeSet.addChange(addPK);
					chsCreateRelPK.put(relationshipName, changeSet);
					counter++;
				//}
			}
			// XXX:Сбор замудренной миграции данных
			if (!migration.isEmpty()) {
				RawSQLChange migrate_change = new RawSQLChange("update "
						+ dbName(relationshipName, tablePrefix) + " set " + migration);
				changeSet = createChangeSet(changeLogId, counter, null, changeLog);
				changeSet.addChange(migrate_change);
				changeSet.setFailOnError(false);
				chsRelDataMigrate.add(changeSet);
				counter++;
			}
		} else {
			if (!chsCreateRelTable.containsKey(relationshipName)) {
				CreateTableChange createTable = new CreateTableChange();
				createTable.setTableName(relationshipName);
				List<ColumnConfig> tableColumns = new ArrayList<ColumnConfig>();

				for (Entry<String, ClassDefRecord> relClass : classes.entrySet()) {
					StoredPropertyMeta pm = relClass.getValue().getKeyProperty();
					AddColumnConfig addColumn = new AddColumnConfig();
					addColumn.setName(relClass.getKey());
					addColumn.setType(getColumnType(pm));
					addColumn.setConstraints(new ConstraintsConfig().setNullable(false));
					tableColumns.add(addColumn);

					if (referentialIntegrity) {
						AddForeignKeyConstraintChange addFK = new AddForeignKeyConstraintChange();
						addFK.setConstraintName(dbName(relationshipName + relClass.getKey(),
																					 foreignKeyPrefix));
						addFK.setBaseTableName(relationshipName);
						addFK.setBaseColumnNames(relClass.getKey());
						addFK.setReferencedTableName(dbName(relClass.getValue().meta.name,
																								tablePrefix));
						addFK.setReferencedColumnNames(dbName(pm.name, columnPrefix));
						addFK.setDeleteCascade(true);
						changeSet = createChangeSet(changeLogId, counter, null, changeLog);
						changeSet.addChange(addFK);
						chsCreateRelFK.add(changeSet);
						counter++;
					}
				}

				createTable.setColumns(tableColumns);
				changeSet = createChangeSet(changeLogId, counter, null, changeLog);
				changeSet.addChange(createTable);
				chsCreateRelTable.put(relationshipName, changeSet);
				counter++;

				AddPrimaryKeyChange addPK = new AddPrimaryKeyChange();
				addPK.setTableName(relationshipName);
				addPK.setColumnNames(curKeyName + "," + otherKeyName);
				changeSet = createChangeSet(changeLogId, counter, null, changeLog);
				changeSet.addChange(addPK);
				chsCreateRelPK.put(relationshipName, changeSet);
				counter++;
			}
		}
		return counter;
	}

	protected List<ChangeSet> generateChangeSets(String changeLogId,
																							 DatabaseChangeLog changeLog,
																							 Connection connection)
																																		 throws Exception {
		// ServiceLocator.getInstance().setResourceAccessor(new
		// ClassLoaderResourceAccessor(ServiceLocator.class.getClassLoader()));

		List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
		DatabaseMetaData existing_meta = connection.getMetaData();

		Map<String, ClassDefRecord> classes = new HashMap<String, ClassDefRecord>();

		ClassDefRecord[] metas = getOrderedMetas(classes);

		ResultSet trs;
		ResultSet crs;
		ResultSet fkrs;

		ChangeSet changeSet;

		Map<String, ChangeSet> chsDropFK = new HashMap<String, ChangeSet>();
		List<ChangeSet> chsDropPK = new ArrayList<ChangeSet>();
		List<ChangeSet> chsCleanPK = new ArrayList<ChangeSet>();
		List<ChangeSet> chsRenameColumns = new ArrayList<ChangeSet>();
		List<ChangeSet> chsModifyType = new ArrayList<ChangeSet>();
		List<ChangeSet> chsCreateTable = new ArrayList<ChangeSet>();
		List<ChangeSet> chsAddColumns = new ArrayList<ChangeSet>();
		List<ChangeSet> chsDataMigrate = new ArrayList<ChangeSet>();
		List<ChangeSet> chsCreateFK = new ArrayList<ChangeSet>();
		List<ChangeSet> chsAddIndex = new ArrayList<ChangeSet>();
		List<ChangeSet> chsFinal = new ArrayList<ChangeSet>();
		List<ChangeSet> chsDropIndex = new ArrayList<ChangeSet>();
		List<ChangeSet> chsCreatePK = new ArrayList<ChangeSet>();

		Map<String, ChangeSet> chsCreateRelTable = new HashMap<String, ChangeSet>();
		Map<String, ChangeSet> chsCreateRelPK = new HashMap<String, ChangeSet>();
		Map<String, ChangeSet> chsDropRelPK = new HashMap<String, ChangeSet>();
		List<ChangeSet> chsRenameRelColumns = new ArrayList<ChangeSet>();
		List<ChangeSet> chsModifyRelColumnType = new ArrayList<ChangeSet>();
		List<ChangeSet> chsAddRelColumns = new ArrayList<ChangeSet>();
		List<ChangeSet> chsRelDataMigrate = new ArrayList<ChangeSet>();
		List<ChangeSet> chsCreateRelFK = new ArrayList<ChangeSet>();

		int counter = 1;

		// обходим меты выкладываемых классов
		for (ClassDefRecord cm : metas) {
			Collection<AddForeignKeyConstraintChange> createFK = new ArrayList<AddForeignKeyConstraintChange>();

			final String tableName = dbName(cm.meta.name, tablePrefix);
			trs = existing_meta.getTables(null, null, tableName, null);
			Map<String, StoredPropertyMeta> prop_metas_list = cm.properties;
			// для суперкласса добавляем колонку дискриминатора, по
			// необходимости
			if (useDiscriminator && cm.getAncestor() == null) {
				prop_metas_list.put(this.discriminatorColumnName, new StoredPropertyMeta(this.discriminatorColumnName,
																									 "",
																									 MetaPropertyType.STRING.getValue(),
																									 this.discriminatorLength));
			}

			StoredPropertyMeta[] prop_metas = prop_metas_list.values().toArray(new StoredPropertyMeta[prop_metas_list.size()]);
			Arrays.sort(prop_metas);

			// если таблица уже существует, ищем в ней изменения
			if (trs.next()) {
				Collection<DropForeignKeyConstraintChange> dropFK = new ArrayList<DropForeignKeyConstraintChange>();
				Collection<RenameColumnChange> renameColumns = new ArrayList<RenameColumnChange>();
				AddColumnChange addColumns = new AddColumnChange();
				addColumns.setTableName(tableName);
				// RawSQLChange dataMigrate = new RawSQLChange();

				String migration = "";

				for (StoredPropertyMeta pm : prop_metas) {
					// формируем имя колонки для бд, для системных полей
					// оставляем неизменным
					final String columnName = (getUseDiscriminator() && pm.name.equals(this.discriminatorColumnName)) ? pm.name : dbName(pm.name, columnPrefix);
					
					// получаем информацию о существующей колонке таблицы
					crs = existing_meta.getColumns(null, null, tableName, columnName);
					StoredPropertyMeta deployed = deployedPM(pm, classes);
					// если колонка существует
					if (crs.next()) {
						boolean need_fk_delete = false;
						boolean need_migration = false;
						boolean need_resize = false;

						Short ss = getColumnSize(pm);

						// проверяем на необходимость преобразований с возможной
						// потерей данных
						// если изменился тип
						if (!cmpPropertyJdbcType(deployed, crs.getInt("DATA_TYPE")))
							need_migration = true;
						// или размер типа
						else if ((ss != null) && isPrecisionedType(crs.getInt("DATA_TYPE"))
								&& crs.getInt("COLUMN_SIZE") > ss)
							need_migration = true;
						// или число знаков после запятой
						else if ((ss != null) && (deployed.decimals != null)
								&& isScaledType(crs.getInt("DATA_TYPE"))
								&& crs.getInt("COLUMN_SIZE") == ss
								&& crs.getInt("DECIMAL_DIGITS") > deployed.decimals)
							need_migration = true;

						// проверяем на необходимость преобразований без потери
						// данных
						if (!need_migration) {
							if (pm.type == MetaPropertyType.DATETIME.getValue() && (crs.getInt("DATA_TYPE") == Types.DATE))
								need_resize = true;							
							if ((ss != null) && isPrecisionedType(crs.getInt("DATA_TYPE"))
									&& crs.getInt("COLUMN_SIZE") < ss)
								need_resize = true;
							else if ((ss != null) && (deployed.decimals != null)
									&& isScaledType(crs.getInt("DATA_TYPE"))
									&& crs.getInt("COLUMN_SIZE") == ss
									&& crs.getInt("DECIMAL_DIGITS") < deployed.decimals)
								need_resize = true;
						}

						// создаем наборы изменений для преобразований с
						// возможной потерей данных
						if (need_migration) {
							RenameColumnChange change = new RenameColumnChange();
							change.setTableName(tableName);
							change.setOldColumnName(columnName);
							change.setNewColumnName(columnName + "_" + changeLogId);
							change.setColumnDataType(columnType(crs,isPrecisionedType(crs.getInt("DATA_TYPE")),isScaledType(crs.getInt("DATA_TYPE"))));
							renameColumns.add(change);

							// если изменилось ключевое поле,
							if (cm.IsKey(pm.name)) {
								// надо сначала дропнуть ключ
								DropPrimaryKeyChange dropPK = new DropPrimaryKeyChange();
								dropPK.setTableName(tableName);
								changeSet = createChangeSet(changeLogId, counter, null,
																						changeLog);
								changeSet.setFailOnError(false);
								changeSet.addChange(dropPK);
								chsDropPK.add(changeSet);
								counter++;

								AddPrimaryKeyChange addPK = new AddPrimaryKeyChange();
								addPK.setTableName(dbName(cm.meta.name, tablePrefix));
								addPK.setColumnNames(dbName(pm.name, columnPrefix));
								changeSet = createChangeSet(changeLogId, counter, null,
																						changeLog);
								changeSet.addChange(addPK);
								chsCreatePK.add(changeSet);
								counter++;

								ModifyDataTypeChange modifyType = new ModifyDataTypeChange();
								modifyType.setTableName(tableName);
								modifyType.setColumnName(columnName);
								modifyType.setNewDataType(columnType(crs,isPrecisionedType(crs.getInt("DATA_TYPE")),isScaledType(crs.getInt("DATA_TYPE"))));
								changeSet = createChangeSet(changeLogId, counter, null,
																						changeLog);
								changeSet.addChange(modifyType);
								changeSet.setFailOnError(false);
								chsCleanPK.add(changeSet);
								counter++;
							}

							if (pm.type == MetaPropertyType.COLLECTION.getValue()) {
								ClassDefRecord items_class = classes.get(pm.items_class);
								if ((pm.back_ref == null || pm.back_ref.trim().isEmpty())
										&& (pm.back_coll == null || pm.back_coll.trim().isEmpty())) {
									counter = establishRelationship(cm, pm, items_class,
																									changeLogId, counter,
																									changeLog, chsCreateRelTable,
																									chsCreateRelPK, chsDropRelPK,
																									chsRenameRelColumns,
																									chsModifyRelColumnType,
																									chsAddRelColumns,
																									chsRelDataMigrate, chsDropFK,
																									chsDropIndex, chsCreateRelFK,
																									existing_meta);
								}
							} else {
								AddColumnConfig column = new AddColumnConfig();
								columnSetup(column, pm, deployed, cm, classes, createFK);
								addColumns.addColumn(column);
								counter = processIndexed(pm, cm, changeLogId, counter,
																				 changeLog, chsAddIndex);
								need_fk_delete = cm.IsKey(pm.name);

								if (pm.type == MetaPropertyType.REFERENCE.getValue()) {
									ResultSet fk = existing_meta.getImportedKeys(null,
																															 null,
																															 dbName(cm.meta.name,
																																			tablePrefix));
									while (fk.next()) {
										if (fk.getString("FKCOLUMN_NAME")
													.equals(dbName(pm.name, columnPrefix))) {
											if (fk.getString("PKTABLE_NAME")
														.equals(dbName(pm.ref_class, tablePrefix))) {
												migration = migration
														+ (migration.isEmpty() ? "" : ",\n")
														+ castDoubleLevelMigrationExpression(dbName(pm.name,
																																				columnPrefix),
																																 dbName(pm.name,
																																				columnPrefix)
																																		 + "_"
																																		 + changeLogId,
																																 dbName(pm.ref_class,
																																				tablePrefix),
																																 dbName(deployed.name,
																																				columnPrefix),
																																 fk.getString("PKCOLUMN_NAME")
																																		 + "_"
																																		 + changeLogId);
											}
											break;
										}
									}
								} else {
									// добавляем скрипт миграции данных в новую
									// колонку с меньшим размером
									migration = migration
											+ (migration.isEmpty() ? "" : ",\n")
											+ columnName
											+ " = "
											+ castExpression(pm, deployed, columnName + "_"
																					 + changeLogId,
																			 crs.getInt("DATA_TYPE"),
																			 crs.getInt("COLUMN_SIZE"),
																			 crs.getInt("DECIMAL_DIGITS"));
								}
							}
							// создаем наборы изменений без потери данных
						} else if (need_resize) {
							ModifyDataTypeChange modifyType = new ModifyDataTypeChange();
							modifyType.setTableName(tableName);
							modifyType.setColumnName(columnName);
							modifyType.setNewDataType(getColumnType(deployed));
							changeSet = createChangeSet(changeLogId, counter, null, changeLog);
							changeSet.addChange(modifyType);
							chsModifyType.add(changeSet);
							counter++;
							need_fk_delete = cm.IsKey(pm.name);

							if (pm.type == MetaPropertyType.REFERENCE.getValue()) {
								AddForeignKeyConstraintChange fk = addFK(cm,
																												 pm,
																												 classes.get(pm.ref_class),
																												 false);
								if (fk != null) {
									changeSet = createChangeSet(changeLogId, counter, null,
																							changeLog);
									changeSet.addChange(fk);
									changeSet.setFailOnError(false);
									chsCreateFK.add(changeSet);
									counter++;
								}
							}
						}

						if (need_fk_delete) {
							fkrs = existing_meta.getExportedKeys(null, null, tableName);
							while (fkrs.next()) {
								DropForeignKeyConstraintChange dfk_change = new DropForeignKeyConstraintChange();
								dfk_change.setBaseTableName(fkrs.getString("FKTABLE_NAME"));
								dfk_change.setConstraintName(fkrs.getString("FK_NAME"));
								dropFK.add(dfk_change);
							}
						}
						// колонка еще не существует, добавляем
					} else {
						if (pm.type == MetaPropertyType.COLLECTION.getValue()) {
							ClassDefRecord items_class = classes.get(pm.items_class);
							if ((pm.back_ref == null || pm.back_ref.trim().isEmpty())
									&& (pm.back_coll == null || pm.back_coll.trim().isEmpty())) {
								counter = establishRelationship(cm, pm, items_class,
																								changeLogId, counter,
																								changeLog, chsCreateRelTable,
																								chsCreateRelPK, chsDropRelPK,
																								chsRenameRelColumns,
																								chsModifyRelColumnType,
																								chsAddRelColumns,
																								chsRelDataMigrate, chsDropFK,
																								chsDropIndex, chsCreateRelFK,
																								existing_meta);
							}
						} else {
							AddColumnConfig column = new AddColumnConfig();
							columnSetup(column, pm, deployed, cm, classes, createFK);
							addColumns.addColumn(column);

							// если это колонка дискриминатора, надо установить
							// в неё значения
							if (useDiscriminator && cm.getAncestor() == null
									&& pm.name.equals(this.discriminatorColumnName)) {
								String script = "";
								// для дискриминатора префикс добавлять не
								// нужно, восстанавливаем исходное имя
								column.setName(pm.name);
								// вложенные классы разбиваем по уровням
								Map<Integer, List<ClassDefRecord>> levels = getInheritanceLevels(classes, cm);
								// обновляем дискриминатор, начиная с дальних
								// наследников
								String keyColumnName = dbName(cm.meta.key, columnPrefix);
								for (int level = levels.size() - 1; level >= 0; level--) {
									for (ClassDefRecord childmeta : levels.get(level)) {
										script += String.format("update %1$s set %2$s = '%3$s' where (%2$s is null) and (%1$s.%4$s in (select %4$s from %5$s));\n",
																						tableName,
																						/* дискриминатор */columnName,
																						/* значение дискриминатора */StringEscapeUtils.escapeSql(childmeta.meta.name),
																						keyColumnName,
																						dbName(childmeta.meta.name,
																									 tablePrefix));
									}
								}
								// и дискриминатор объектов суперкласса
								script += String.format("update %1$s set %2$s = '%3$s' where (%2$s is null)\n",
																				tableName,
																				/* дискриминатор */columnName,
																				/* значение дискриминатора */StringEscapeUtils.escapeSql(cm.meta.name));
								changeSet = sqlToChangeset(changeLog, changeLogId, counter,
																					 script);
								chsDataMigrate.add(changeSet);
								counter++;

								// AddNotNullConstraintChange change = new
								// AddNotNullConstraintChange();
								// change.setColumnName(columnName);
								// change.setTableName(tableName);
								// changeSet = createChangeSet(changeLogId,
								// counter, null, changeLog);
								// changeSet.addChange(change);
								// changeSet.setFailOnError(false);
								// chsFinal.add(changeSet);
								// counter++;
							}
						}
					}
				}

				// если это не суперкласс
				if (cm.getAncestor() != null) {
					// получаем ключ
					StoredPropertyMeta pm = cm.getKeyProperty();
					crs = existing_meta.getColumns(null,
																				 null,
																				 tableName,
																				 dbName(pm.name, columnPrefix));
					if (crs.next()) {
						// ключ еще не существует, добавляем
					} else {
						AddColumnConfig column = new AddColumnConfig();
						columnSetup(column, pm, pm, cm, classes, createFK);
						addColumns.addColumn(column);
					}
				}

				// добавляем наборы изменений для внешних ключей
				if (dropFK.size() > 0) {
					for (DropForeignKeyConstraintChange ch : dropFK) {
						if (!chsDropFK.containsKey(ch.getConstraintName())) {
							changeSet = createChangeSet(changeLogId, counter, null, changeLog);
							changeSet.addChange(ch);
							chsDropFK.put(ch.getConstraintName(), changeSet);
							counter++;

							ResultSet irs = existing_meta.getIndexInfo(null, null,
																												 ch.getBaseTableName(),
																												 false, true);
							while (irs.next())
								if (irs.getString("INDEX_NAME").equals(ch.getConstraintName())) {
									DropIndexChange dropIndex = new DropIndexChange();
									dropIndex.setTableName(ch.getBaseTableName());
									dropIndex.setIndexName(ch.getConstraintName());
									changeSet = createChangeSet(changeLogId, counter, null,
																							changeLog);
									changeSet.addChange(dropIndex);
									changeSet.setFailOnError(false);
									chsDropIndex.add(changeSet);
									counter++;
								}
						}
					}
				}

				// добавляем наборы изменений для новых колонок
				if (addColumns.getColumns().size() > 0) {
					changeSet = createChangeSet(changeLogId, counter, null, changeLog);
					changeSet.addChange(addColumns);
					chsAddColumns.add(changeSet);
					counter++;
				}

				// добавляем наборы изменений для переименованных колонок
				if (renameColumns.size() > 0) {
					for (Change ch : renameColumns) {
						changeSet = createChangeSet(changeLogId, counter, null, changeLog);
						changeSet.addChange(ch);
						chsRenameColumns.add(changeSet);
						counter++;
					}
				}

				// добавляем набор изменений со скриптом миграции
				if (!migration.isEmpty()) {
					changeSet = sqlToChangeset(changeLog, changeLogId, counter,
																		 String.format("update %s set %s",
																									 tableName, migration));
					chsDataMigrate.add(changeSet);
					counter++;
				}
				// таблицы не существует, добавляем
			} else {
				CreateTableChange change = new CreateTableChange();
				change.setTableName(tableName);
				List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
				ColumnConfig column;
				String keypropname = null;
				if (cm.getAncestor() != null) {
					StoredPropertyMeta pm = cm.getKeyProperty();
					keypropname = pm.name;
					column = new ColumnConfig();
					columnSetup(column, pm, pm, cm, classes, createFK);
					columns.add(column);
				}

				for (StoredPropertyMeta pm : prop_metas) {
					if (pm.type == MetaPropertyType.COLLECTION.getValue()) {
						ClassDefRecord items_class = classes.get(pm.items_class);
						if ((pm.back_ref == null || pm.back_ref.trim().isEmpty())
								&& (pm.back_coll == null || pm.back_coll.trim().isEmpty())) {
							counter = establishRelationship(cm, pm, items_class, changeLogId,
																							counter, changeLog,
																							chsCreateRelTable,
																							chsCreateRelPK, chsDropRelPK,
																							chsRenameRelColumns,
																							chsModifyRelColumnType,
																							chsAddRelColumns,
																							chsRelDataMigrate, chsDropFK,
																							chsDropIndex, chsCreateRelFK,
																							existing_meta);
						}
					} else {
						if (!pm.name.equals(keypropname)) {
							StoredPropertyMeta deployed = deployedPM(pm, classes);
							column = new ColumnConfig();
							columnSetup(column, pm, deployed, cm, classes, createFK);
							if (useDiscriminator && cm.getAncestor() == null
									&& pm.name.equals(this.discriminatorColumnName))
								// для дискриминатора префикс добавлять не
								// нужно, восстанавливаем исходное имя
								column.setName(pm.name);
							columns.add(column);
							counter = processIndexed(pm, cm, changeLogId, counter, changeLog,
																			 chsAddIndex);
						}
					}
				}

				change.setColumns(columns);
				changeSet = createChangeSet(changeLogId, counter, null, changeLog);
				changeSet.addChange(change);
				chsCreateTable.add(changeSet);
				counter++;
			}

			// добавляем наборы изменений для создаваемых внешних ключей
			if (createFK.size() > 0) {
				for (Change ch : createFK) {
					changeSet = createChangeSet(changeLogId, counter, null, changeLog);
					changeSet.addChange(ch);
					changeSet.setFailOnError(false);
					chsCreateFK.add(changeSet);
					counter++;
				}
			}
		}

		changeSets.addAll(chsDropFK.values());
		changeSets.addAll(chsCleanPK);
		changeSets.addAll(chsDropPK);
		changeSets.addAll(chsRenameColumns);
		changeSets.addAll(chsModifyType);
		changeSets.addAll(chsCreateTable);
		changeSets.addAll(chsAddColumns);
		changeSets.addAll(chsDataMigrate);
		changeSets.addAll(chsAddIndex);
		changeSets.addAll(chsCreateFK);
		changeSets.addAll(chsFinal);
		changeSets.addAll(chsCreatePK);

		changeSets.addAll(chsDropRelPK.values());
		changeSets.addAll(chsRenameRelColumns);
		changeSets.addAll(chsModifyRelColumnType);
		changeSets.addAll(chsCreateRelTable.values());
		changeSets.addAll(chsAddRelColumns);
		changeSets.addAll(chsRelDataMigrate);
		changeSets.addAll(chsCreateRelPK.values());
		changeSets.addAll(chsCreateRelFK);

		classes.clear();

		return changeSets;
	}

	Map<Integer, List<ClassDefRecord>> getInheritanceLevels(Map<String, ClassDefRecord> classes,
																													ClassDefRecord parent) {
		Map<Integer, List<ClassDefRecord>> result = new HashMap<Integer, List<ClassDefRecord>>();
		getInheritanceLevels(result, classes, parent, 0);
		return result;
	}

	void getInheritanceLevels(Map<Integer, List<ClassDefRecord>> levels,
														Map<String, ClassDefRecord> classes,
														ClassDefRecord parent, int level) {
		String parentName = parent == null ? "" : parent.meta.name;
		// ищем классы с указанным родителем
		for (ClassDefRecord meta : classes.values()) {
			// сравниваем на всякий случай по имени
			ClassDefRecord anc = meta.getAncestor();
			String ancName = anc == null ? "" : anc.meta.name;
			if (parentName.equals(ancName)) {
				// добавляем найденный класс в соотв. уровень
				if (!levels.containsKey(level))
					levels.put(level, new LinkedList<ClassDefRecord>());
				levels.get(level).add(meta);
				getInheritanceLevels(levels, classes, meta, level + 1);
			}
		}
	}

	ChangeSet sqlToChangeset(DatabaseChangeLog changeLog, String changeLogId,
													 int counter, String sql) {
		ChangeSet changeSet;
		RawSQLChange migrate_change = new RawSQLChange(sql);
		changeSet = createChangeSet(changeLogId, counter, null, changeLog);
		changeSet.addChange(migrate_change);
		changeSet.setFailOnError(false);
		return changeSet;
	}

	public String BuildScript(Connection c, String origin) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String changeLogId = dateFormat.format(new Date());
		String fn = dbPatchDirectory + File.separator + "rev-" + changeLogId
				+ ".xml";
		if (origin != null && !origin.isEmpty())
			fn = dbPatchDirectory + File.separator + origin + "-rev-" + changeLogId
					+ ".xml";

		DatabaseChangeLog changeLog = new DatabaseChangeLog(fn);
		List<ChangeSet> changeSets = generateChangeSets(changeLogId, changeLog, c);
		if (changeSets.size() > 0) {
			File result = new File(fn);
			if (!result.getParentFile().exists())
				result.getParentFile().mkdirs();
			FileOutputStream stream = new FileOutputStream(result);
			ChangeLogSerializer xmlSerializer = new XMLChangeLogSerializer();
			xmlSerializer.write(changeSets, stream);
			stream.flush();
			stream.close();
			System.out.println(String.format("Liquibase script file '%s' created successfully",
																			 fn));
			return fn;
		}
		return null;
	}

	public void Deploy(Connection c, String origin) throws Exception {
		Deploy(c, origin, false);
	}

	public void Deploy(Connection c, String origin, boolean forceUnsafeUpdate)
																																						throws Exception {
		String fn = BuildScript(c, origin);
		if (fn != null) {
			Liquibase l = new Liquibase(fn,
																	new FileSystemResourceAccessor(new File(fn).getParent()),
																	new JdbcConnection(c));
			ServiceLocator.getInstance().addPackageToScan("liquibase.changelog");
			boolean isSafe = l.isSafeToRunUpdate();
			if (forceUnsafeUpdate || isSafe) {
				if (!isSafe)
					System.out.println("WARNING: Starting unsafe update");
				System.out.println("Executing liquibase script file " + fn);
				l.update("");
			} else {
				throw new IonException("Небезопасное обновление");
			}
		}
	}
}
