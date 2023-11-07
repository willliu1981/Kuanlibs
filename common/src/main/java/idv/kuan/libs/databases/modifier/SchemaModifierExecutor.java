package idv.kuan.libs.databases.modifier;

import java.sql.Connection;

public interface   SchemaModifierExecutor {

    void execute(Connection connection,int appVersion);

}
