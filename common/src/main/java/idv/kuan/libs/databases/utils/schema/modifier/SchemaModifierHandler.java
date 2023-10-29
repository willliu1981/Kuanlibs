package idv.kuan.libs.databases.utils.schema.modifier;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import idv.kuan.libs.databases.utils.SQLiteSchemaModifierUtil;

public class SchemaModifierHandler {
    private int appVersion;
    private Connection connection;
    private List<SchemaModifier> modifiers = new ArrayList<>();

    public SchemaModifierHandler(Connection connection, int appVersion) {
        this.connection = connection;
        this.appVersion = appVersion;


    }

    public static class SchemaModifierCreator {
        private SchemaModifierHandler handler;
        String constructionSql;

        private SchemaModifierCreator(SchemaModifierHandler schemaModifierHandler) {
            this.handler = schemaModifierHandler;
        }

        public void setConstructionSql(String sql) {
            this.constructionSql = sql;
        }

        public SchemaModifier createSchemaModifier() {

            return new TableSchemaModifier(handler.connection, handler.appVersion, constructionSql);
        }


    }


    public SchemaModifierCreator getSchemaModifierCreator() {
        return new SchemaModifierCreator(this);
    }


    public void execute() {
        if (!modifiers.isEmpty()) {
            this.modifiers.forEach(SchemaModifier::execute);

            //最後必做update database version
            SQLiteSchemaModifierUtil.updateDBVersion(connection, appVersion);
        }

    }

    public void addSchemaModifier(SchemaModifier schemaModifier) {
        this.modifiers.add(schemaModifier);
    }

}
