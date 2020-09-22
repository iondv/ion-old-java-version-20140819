package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;
import liquibase.util.JdbcUtils;
import liquibase.util.csv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MissingDataChangeGenerator implements MissingObjectChangeGenerator {

    
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Data.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
                Table.class
        };
    }

    
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{
                PrimaryKey.class, ForeignKey.class, Index.class
        };
    }

    
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl outputControl, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Data data = (Data) missingObject;

            Table table = data.getTable();
            if (referenceDatabase.isLiquibaseObject(table)) {
                return null;
            }

            String sql = "SELECT * FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());

            stmt = ((JdbcConnection) referenceDatabase.getConnection()).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
            rs = stmt.executeQuery(sql);

            List<String> columnNames = new ArrayList<String>();
            for (int i=0; i< rs.getMetaData().getColumnCount(); i++) {
                columnNames.add(rs.getMetaData().getColumnName(i+1));
            }

            List<Change> changes = new ArrayList<Change>();
            while (rs.next()) {
                InsertDataChange change = new InsertDataChange();
                if (outputControl.isIncludeCatalog()) {
                    change.setCatalogName(table.getSchema().getCatalogName());
                }
                if (outputControl.isIncludeSchema()) {
                    change.setSchemaName(table.getSchema().getName());
                }
                change.setTableName(table.getName());

                // loop over all columns for this row
                for (int i = 0; i < columnNames.size(); i++) {
                    ColumnConfig column = new ColumnConfig();
                    column.setName(columnNames.get(i));

                    Object value = JdbcUtils.getResultSetValue(rs, i + 1);
                    if (value == null) {
                        column.setValue(null);
                    } else if (value instanceof Number) {
                        column.setValueNumeric((Number) value);
                    } else if (value instanceof Boolean) {
                        column.setValueBoolean((Boolean) value);
                    } else if (value instanceof Date) {
                        column.setValueDate((Date) value);
                    } else { // string
                        column.setValue(value.toString().replace("\\", "\\\\"));
                    }

                    change.addColumn(column);

                }

                // for each row, add a new change
                // (there will be one group per table)
                changes.add(change);
            }

            return changes.toArray(new Change[changes.size()]);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) { }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) { }
            }
        }
    }
}
