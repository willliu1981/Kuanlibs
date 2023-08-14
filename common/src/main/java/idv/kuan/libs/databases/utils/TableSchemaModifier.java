package idv.kuan.libs.databases.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class TableSchemaModifier {
    private List<SchemaModifierExecutor> list = new ArrayList<>();

    public void addSchemaModifierExecutor(SchemaModifierExecutor schemaModifierExecutor) {
        this.list.add(schemaModifierExecutor);
    }

    public void updateDBVersion(Connection connection, int databaseVersion) {
        for (SchemaModifierExecutor se : list) {
            se.execute();
        }

        TableSchemaModifiers.updateDBVersion(connection, databaseVersion);
    }

}
