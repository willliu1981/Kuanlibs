package idv.kuan.libs.databases.utils.schema.modifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    public static class SchemaModifierBuilder {
        private SchemaModifierHandler handler;
        String constructionSql;

        private SchemaModifierBuilder(SchemaModifierHandler schemaModifierHandler) {
            this.handler = schemaModifierHandler;
        }

        public void setConstructionSql(String sql) {
            this.constructionSql = sql;
        }

        public <T extends  SchemaModifierImpl> T createSchemaModifier(Class<T> schemaModifierClass) {
            try {
                Constructor<T> constructor = schemaModifierClass.getConstructor(Connection.class, int.class, String.class);
                return (T) constructor.newInstance(handler.connection, handler.appVersion, constructionSql);

            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        }


    }


    public SchemaModifierBuilder getSchemaModifierCreator() {
        return new SchemaModifierBuilder(this);
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
