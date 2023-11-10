package idv.kuan.libs.databases.schema.modifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class SchemaModifierHandler {
    private int appVersion;
    private Connection connection;
    private List<SchemaModifier> modifiers = new ArrayList<>();
    private SchemaModifierBuilder builder;

    public SchemaModifierHandler(Connection connection, int appVersion) {
        this.connection = connection;
        this.appVersion = appVersion;


    }

    /**
     * 用來產生SchemaModifier實例,
     * SchemaModifier 可以用來執行SQL語句,以修改Schema表格內容,
     * 最後請使用SchemaModifierHandler.addSchemaModifier 將SchemaModifier實例加住list中,
     * 並執行SchemaModifierHandler.execute 開始逐一update table,
     * 該execute 在最後終會執行SQLiteSchemaModifierUtil.updateDBVersion,用來記綠app version 版本
     */
    public static class SchemaModifierBuilder {
        private SchemaModifierHandler handler;
        private String constructionSql;
        private String tableName;


        private SchemaModifierBuilder(SchemaModifierHandler schemaModifierHandler) {
            this.handler = schemaModifierHandler;
        }

        public SchemaModifierBuilder setConstructionSql(String sql) {
            this.constructionSql = sql;
            return this;
        }

        public SchemaModifierBuilder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public <T extends SchemaModifierImpl> T createSchemaModifier(Class<T> schemaModifierClass) {
            try {
                Constructor<T> constructor = schemaModifierClass.getConstructor(Connection.class, int.class, String.class, String.class);
                T t = constructor.newInstance(handler.connection, handler.appVersion, constructionSql, tableName);

                initParams();
                return t;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void initParams() {
            this.constructionSql = null;
            this.tableName = null;
        }


    }


    /**
     * 取得 SchemaModifierBuilder,進一步產生SchemaModifier實例,
     * SchemaModifier 可以用來執行SQL語句,以修改Schema表格內容
     *
     * @return
     */
    public SchemaModifierBuilder getSchemaModifierBuilder() {
        if (this.builder == null) {
            this.builder = new SchemaModifierBuilder(this);
        }
        return this.builder;
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
