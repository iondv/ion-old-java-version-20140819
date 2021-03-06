package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class RecentChangesWriter extends HTMLWriter {

    public RecentChangesWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "recent"), database);
    }

    
    protected String createTitle(Object object) {
        return "Recent Changes";
    }

    
    protected void writeBody(FileWriter fileWriter, Object object, List<Change> ranChanges, List<Change> changesToRun) throws IOException, DatabaseHistoryException, DatabaseException {
        writeCustomHTML(fileWriter, object, ranChanges, database);
        writeChanges("Most Recent Changes", fileWriter, ranChanges);
    }

    
    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
