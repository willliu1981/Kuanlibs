package idv.kuan.libs.utils.schema.modifier;

import java.sql.Connection;

public abstract class SchemaModifierImpl implements SchemaModifier {
    String constructionSql;
    Connection connection;
    int appVersion;

    public SchemaModifierImpl(Connection connection, int appVersion, String constructionSql) {
        this.connection = connection;
        this.appVersion = appVersion;
        this.constructionSql = constructionSql;
    }

}
