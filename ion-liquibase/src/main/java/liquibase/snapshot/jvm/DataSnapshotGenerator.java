package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Data;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.List;

public class DataSnapshotGenerator extends JdbcSnapshotGenerator {

    public DataSnapshotGenerator() {
        super(Data.class, new Class[]{Table.class});
    }

    
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Data.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            try {

                Data exampleData = new Data().setTable(table);
                table.setAttribute("data", exampleData);
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
    }
}