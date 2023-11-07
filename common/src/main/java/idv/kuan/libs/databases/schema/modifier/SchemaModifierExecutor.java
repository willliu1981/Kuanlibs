package idv.kuan.libs.databases.schema.modifier;

import java.sql.Connection;

public interface   SchemaModifierExecutor {

    void execute(Connection connection,int appVersion);

}
