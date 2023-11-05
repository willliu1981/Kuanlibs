package idv.kuan.libs.utils.schema.modifier;

import java.sql.Connection;

public interface   SchemaModifierExecutor {

    void execute(Connection connection,int appVersion);

}
