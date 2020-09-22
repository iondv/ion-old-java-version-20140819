package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.*;

public class SnapshotControl implements LiquibaseSerializable {

    private Set<Class<? extends DatabaseObject>> types;

    public SnapshotControl(Database database) {
        setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
    }

    public SnapshotControl(Database database, Class<? extends DatabaseObject>... types) {
        if (types == null || types.length == 0) {
            setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
        } else {
            setTypes(new HashSet<Class<? extends DatabaseObject>>(Arrays.asList(types)), database);
        }
    }

    public SnapshotControl(Database database, String types) {
        setTypes(DatabaseObjectFactory.getInstance().parseTypes(types), database);
    }

    
    public String getSerializedObjectName() {
        return "snapshotControl";
    }

    
    public Set<String> getSerializableFields() {
        return new HashSet<String>(Arrays.asList("includedType"));
    }

    
    public Object getSerializableFieldValue(String field) {
        if (field.equals("includedType")) {
            SortedSet<String> types = new TreeSet<String>();
            for (Class type : this.getTypesToInclude()) {
                types.add(type.getName());
            }
            return types;
        } else {
            throw new UnexpectedLiquibaseException("Unknown field "+field);
        }
    }

    
    public SerializationType getSerializableFieldType(String field) {
        if (field.equals("includedType")) {
            return SerializationType.NESTED_OBJECT;
        } else {
            throw new UnexpectedLiquibaseException("Unknown field "+field);
        }
    }

    
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    private void setTypes(Set<Class<? extends DatabaseObject>> types, Database database) {
        this.types = new HashSet<Class<? extends DatabaseObject>>();
        for (Class<? extends DatabaseObject> type : types) {
            addType(type, database);
        }
    }

    public boolean addType(Class<? extends DatabaseObject> type, Database database) {
        boolean added = this.types.add(type);
        if (added) {
            for (Class<? extends DatabaseObject> container : SnapshotGeneratorFactory.getInstance().getContainerTypes(type, database)) {
                addType(container, database);
            }
        }

        return added;

    }

    public Set<Class<? extends DatabaseObject>> getTypesToInclude() {
        return types;
    }

    public boolean shouldInclude(Class<? extends DatabaseObject> type) {
        return types.contains(type);
    }
}
