package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds functionality for setting the sequence to be owned by the column with the default value
 */
public class AddDefaultValueGeneratorPostgres extends AddDefaultValueGenerator {

    
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    
    public Sql[] generateSql(final AddDefaultValueStatement statement, final Database database,
                             final SqlGeneratorChain sqlGeneratorChain) {

        if (!(statement.getDefaultValue() instanceof SequenceNextValueFunction)) {
            return super.generateSql(statement, database, sqlGeneratorChain);
        }

        List<Sql> commands = new ArrayList<Sql>(Arrays.asList(super.generateSql(statement, database, sqlGeneratorChain)));
        // for postgres, we need to also set the sequence to be owned by this table for true serial like functionality.
        // this will allow a drop table cascade to remove the sequence as well.
        SequenceNextValueFunction sequenceFunction = (SequenceNextValueFunction) statement.getDefaultValue();

        Sql alterSequenceOwner = new UnparsedSql("ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(),
                statement.getSchemaName(), sequenceFunction.getValue()) + " OWNED BY " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "."
                + database.escapeObjectName(statement.getColumnName(), Column.class),
                getAffectedColumn(statement),
                getAffectedSequence(sequenceFunction));
        commands.add(alterSequenceOwner);
        return commands.toArray(new Sql[commands.size()]);
    }

    protected Sequence getAffectedSequence(SequenceNextValueFunction sequenceFunction) {
        return new Sequence().setName(sequenceFunction.getValue());
    }
}
