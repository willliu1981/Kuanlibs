package idv.kuan.libs.utils.schema.modifier;

import java.sql.Connection;

public abstract class SchemaModifierImpl implements SchemaModifier {
    protected String constructionSql;
    protected String tableName;
    protected Connection connection;
    protected int appVersion;

    public SchemaModifierImpl(Connection connection, int appVersion, String constructionSql, String tableName) {
        this.connection = connection;
        this.appVersion = appVersion;
        this.constructionSql = constructionSql;
        this.tableName = tableName;
    }

}
