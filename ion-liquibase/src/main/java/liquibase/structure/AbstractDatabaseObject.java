package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtils;

import java.util.*;

public abstract class AbstractDatabaseObject implements DatabaseObject {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    private UUID snapshotId;

    
    public String getObjectTypeName() {
        return StringUtils.lowerCaseFirst(getClass().getSimpleName());
    }

    
    public UUID getSnapshotId() {
        return snapshotId;
    }

    
    public void setSnapshotId(UUID snapshotId) {
        if (snapshotId == null) {
            throw new UnexpectedLiquibaseException("Must be a non null uuid");
        }
        if (this.snapshotId != null) {
            throw new UnexpectedLiquibaseException("snapshotId already set");
        }
        this.snapshotId = snapshotId;
    }

    
    public boolean snapshotByDefault() {
        return true;
    }

    
    public int compareTo(Object o) {
        return this.getName().compareTo(((AbstractDatabaseObject) o).getName());
    }

    
    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    
    public <T> T getAttribute(String attribute, Class<T> type) {
        return (T) attributes.get(attribute);
    }

    
    public DatabaseObject setAttribute(String attribute, Object value) {
        if (value == null) {
            attributes.remove(attribute);
        } else {
            attributes.put(attribute, value);
        }
        return this;
    }

    
    public String getSerializedObjectName() {
        return getObjectTypeName();
    }

    
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    
    public Set<String> getSerializableFields() {
        TreeSet<String> fields = new TreeSet<String>(attributes.keySet());
        fields.add("snapshotId");
        return fields;
    }

    
    public Object getSerializableFieldValue(String field) {
        if (field.equals("snapshotId")) {
            return snapshotId;
        }
        if (!attributes.containsKey(field)) {
            throw new UnexpectedLiquibaseException("Unknown field "+field);
        }
        Object value = attributes.get(field);
        if (value instanceof DatabaseObject) {
            try {
                DatabaseObject clone = (DatabaseObject) value.getClass().newInstance();
                clone.setName(((DatabaseObject) value).getName());
                clone.setSnapshotId(((DatabaseObject) value).getSnapshotId());
                return clone;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return value;
    }

    
    public LiquibaseSerializable.SerializationType getSerializableFieldType(String field) {
        if (getSerializableFieldValue(field) instanceof DatabaseObject) {
            return LiquibaseSerializable.SerializationType.NAMED_FIELD;
        } else {
            return LiquibaseSerializable.SerializationType.NAMED_FIELD;
        }
    }

    
    public String toString() {
        return getName();
    }
}
