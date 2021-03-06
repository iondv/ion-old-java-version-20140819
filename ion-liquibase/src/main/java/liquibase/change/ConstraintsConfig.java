package liquibase.change;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.ReflectionSerializer;
import liquibase.util.StringUtils;

import java.util.Set;

/**
 * The standard configuration used by Change classes to represent a constraints on a column.
 */
public class ConstraintsConfig implements LiquibaseSerializable {

    private Boolean nullable;
    private Boolean primaryKey;
    private String primaryKeyName;
    private String primaryKeyTablespace;
    private String references;
    private String referencedTableName;
    private String referencedColumnNames;
    private Boolean unique;
    private String uniqueConstraintName;
    private String checkConstraint;
    private Boolean deleteCascade;
    private String foreignKeyName;
    private Boolean initiallyDeferred;
    private Boolean deferrable;

    /**
     * Returns if the column should be nullable. Returns null if unspecified.
     */
    public Boolean isNullable() {
        return nullable;
    }

    public ConstraintsConfig setNullable(Boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    /**
     * Set the nullable parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setNullable(String nullable) {
        this.nullable = parseBoolean(nullable);

        return this;
    }


    /**
     * Returns true if the column should be part of the primary key. Returns null if unspecified
     */
    public Boolean isPrimaryKey() {
        return primaryKey;
    }

    public ConstraintsConfig setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    /**
     * Set the primaryKey parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setPrimaryKey(String primaryKey) {
        this.primaryKey = parseBoolean(primaryKey);

        return this;
    }


    /**
     * Returns the name to use for the primary key constraint. Returns null if not specified
     */
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public ConstraintsConfig setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
        return this;
    }

    /**
     * Returns the "references" clause to use for the foreign key. Normally a string of the format TABLE(COLUMN_NAME).
     * Returns null if not specified
     */
    public String getReferences() {
        return references;
    }

    public ConstraintsConfig setReferences(String references) {
        this.references = references;
        return this;
    }

    /**
     * Returns if the column is part of a unique constraint. Returns null if not specified
     */
    public Boolean isUnique() {
        return unique;
    }

    public ConstraintsConfig setUnique(Boolean unique) {
        this.unique = unique;
        return this;
    }

    /**
     * Set the unique parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setUnique(String unique) {
        this.unique = parseBoolean(unique);

        return this;
    }


    /**
     * Returns the name to use for the unique constraint. Returns null if not specified
     */
    public String getUniqueConstraintName() {
        return uniqueConstraintName;
    }

    public ConstraintsConfig setUniqueConstraintName(String uniqueConstraintName) {
        this.uniqueConstraintName = uniqueConstraintName;
        return this;
    }

    /**
     * Returns the check constraint to use on this column. Returns null if not specified
     */
    public String getCheckConstraint() {
        return checkConstraint;
    }

    public ConstraintsConfig setCheckConstraint(String checkConstraint) {
        this.checkConstraint = checkConstraint;
        return this;
    }


    /**
     * Returns if a foreign key defined for this column should cascade deletes. Returns null if not specified.
     */
    public Boolean isDeleteCascade() {
        return deleteCascade;
    }

    public ConstraintsConfig setDeleteCascade(Boolean deleteCascade) {
        this.deleteCascade = deleteCascade;
        return this;
    }

    /**
     * Set the deleteCascade parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setDeleteCascade(String deleteCascade) {
        this.deleteCascade = parseBoolean(deleteCascade);

        return this;
    }


    /**
     * Returns the name to use for the columns foreign key constraint. Returns null if not specified.
     */
    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public ConstraintsConfig setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
        return this;
    }

    /**
     * Returns if a foreign key defined for this column should be "initially deferred"c. Returns null if not specified.
     */
    public Boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public ConstraintsConfig setInitiallyDeferred(Boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    /**
     * Set the initiallyDeferred parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setInitiallyDeferred(String initiallyDeferred) {
        this.initiallyDeferred = parseBoolean(initiallyDeferred);

        return this;
    }


    /**
     * Returns if a foreign key defined for this column should deferrable. Returns null if not specified.
     */
    public Boolean isDeferrable() {
        return deferrable;
    }

    public ConstraintsConfig setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

    /**
     * Set the deferrable parameter based on the passed string.
     * Sets true if the passed string is 1 or true or TRUE.
     * Sets false if the passed string is 0 or false or FALSE.
     * Sets null if the passed string is null or "null" or "NULL".
     * Throws an {@link UnexpectedLiquibaseException} if a different value is passed
     */
    public ConstraintsConfig setDeferrable(String deferrable) {
        this.deferrable = parseBoolean(deferrable);

        return this;
    }

    /**
     * Returns the tablespace to use for the defined primary key. Returns null if not specified.
     */
    public String getPrimaryKeyTablespace() {
		return primaryKeyTablespace;
	}

	public ConstraintsConfig setPrimaryKeyTablespace(String primaryKeyTablespace) {
		this.primaryKeyTablespace = primaryKeyTablespace;
        return this;
	}

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(String referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    private Boolean parseBoolean(String value) {
        value = StringUtils.trimToNull(value);
        if (value == null || value.equalsIgnoreCase("null")) {
            return null;
        } else {
            if (value.equalsIgnoreCase("true") || value.equals("1")) {
                return true;
            } else if (value.equalsIgnoreCase("false") || value.equals("0")) {
                return false;
            } else {
                throw new UnexpectedLiquibaseException("Unparsable boolean value: "+value);
            }

        }
    }

    
    public String getSerializedObjectName() {
        return "constraints";
    }

    
    public Set<String> getSerializableFields() {
        return ReflectionSerializer.getInstance().getFields(this);
    }

    
    public Object getSerializableFieldValue(String field) {
        return ReflectionSerializer.getInstance().getValue(this, field);
    }

    
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}