package idv.kuan.libs.databases.schema.modifier;

import java.util.ArrayList;
import java.util.List;

import idv.kuan.libs.databases.DBFactoryConfiguration;
import idv.kuan.libs.utils.VersionHelper;

public class DatabaseSchemaUtils {

    public interface UpdateSchemaExecutor {


        /**
         * 使用modifierBuilder 創建SchemaModifier實例後,使用modifiers添加SchemaModifier所有實例即可
         *
         * @param modifierBuilder
         * @param modifiers
         */
        public void execute(SchemaModifierHandler.SchemaModifierBuilder modifierBuilder, List<SchemaModifier> modifiers);
    }


    /**
     * 重要:使用該方法前需使用DBFactoryConfiguration註冊database 相關資訊,
     * UpdateSchemaExecutor 只有一個execute方法,
     * 並且提供有SchemaModifierHandler.SchemaModifierBuilder工具來創造SchemaModifier實例
     *
     * @param executor
     */
    public static void checkAndUpdateSchema(VersionHelper helper, UpdateSchemaExecutor executor) {
        SchemaModifierHandler handler = new SchemaModifierHandler(DBFactoryConfiguration.getFactory().getConnection(), helper.getVersionCode());
        List<SchemaModifier> modifiers = new ArrayList<>();
        executor.execute(handler.getSchemaModifierBuilder(), modifiers);
        modifiers.forEach(m -> handler.addSchemaModifier(m));
        handler.execute();
    }


}
