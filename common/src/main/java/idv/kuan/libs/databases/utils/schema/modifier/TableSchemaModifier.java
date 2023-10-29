package idv.kuan.libs.databases.utils.schema.modifier;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import idv.kuan.libs.databases.utils.SQLiteSchemaModifiers;

public class TableSchemaModifier implements SchemaModifier {

    String tableName;
    String constructionSql;
    Connection connection;
    int appVersion;
    String currentColumns;
    String selectedColumns;


    public TableSchemaModifier(Connection connection, int appVersion, String constructionSql) {
        this.constructionSql = constructionSql;
        this.connection = connection;
        this.appVersion = appVersion;
    }


    @Override
    public void execute() {
        SQLiteSchemaModifiers.ColumnsMappingSql columnsMappingSql = new SQLiteSchemaModifiers.ColumnsMappingSql(tableName);
        columnsMappingSql.createInsertIntoSQL(currentColumns, selectedColumns);
        createOrUpdateTableWithDataMigration(connection, appVersion, tableName, constructionSql, columnsMappingSql);

    }


    /**
     * @param connection
     * @param appVersion
     * @param tableName
     * @param createSql
     * @param partMigrateSqlMapping 以冒號 ":" 分隔前面和後面的column語句,前為目前要使用的columns,後為select 的 columns
     * @return
     */
    public boolean createOrUpdateTableWithDataMigration(
            Connection connection, int appVersion, String tableName, String createSql, String
            partMigrateSqlMapping) {

        String[] split = partMigrateSqlMapping.split(":");

        SQLiteSchemaModifiers.ColumnsMappingSql schemaModifierSQL = new SQLiteSchemaModifiers.ColumnsMappingSql(tableName);
        schemaModifierSQL.createInsertIntoSQL(split[0], split[1]);

        return createOrUpdateTableWithDataMigration(connection, appVersion, tableName, createSql, schemaModifierSQL);
    }

    /**
     * 更新每個資料結構後,務必執行updateDBVersion
     *
     * @param connection
     * @param appVersion 取得方法:packageManager.getPackageInfo(getPackageName(), 0).versionCode
     * @param tableName
     * @param createSql
     * @return
     */
    public boolean createOrUpdateTableWithDataMigration(
            Connection connection, int appVersion, String tableName, String
            createSql, SQLiteSchemaModifiers.ColumnsMappingSql insertIntoSql) {

        Boolean isTableExist = SQLiteSchemaModifiers.isTableExist(connection, tableName);
        boolean isUpdated = false;
        if (isTableExist != null) {
            if (!isTableExist) {//不存在table,創建新的
                SQLiteSchemaModifiers.createNew(connection, createSql);
            } else {//有同名table,則開始覆寫
                //DB版本table 是否存在
                Boolean isDBVersionTableExist = SQLiteSchemaModifiers.isTableExist(connection, SQLiteSchemaModifiers.DB_VERSION_TABLE);
                if (!isDBVersionTableExist) {//DB版本table 不存在則創建新的DB版本table
                    String DBVersionTableCreateSql = "CREATE TABLE \"" + SQLiteSchemaModifiers.DB_VERSION_TABLE + "\" ( " +
                            " \"" + SQLiteSchemaModifiers.TABLE_COLUMN_DATABASE_VERSION + "\" INTEGER DEFAULT -1 )";
                    SQLiteSchemaModifiers.createNew(connection, DBVersionTableCreateSql);

                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement("insert into " + SQLiteSchemaModifiers.DB_VERSION_TABLE + " values(-1)");
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                int dbVersion = SQLiteSchemaModifiers.getDBVersion(connection);

                if (dbVersion == -2) {//無紀錄時,則重建新的db version table 結構,和鍵入一筆值為-1的default 紀錄
                    SQLiteSchemaModifiers.initializeDatabaseVersionTable(connection);
                    dbVersion = SQLiteSchemaModifiers.getDBVersion(connection);
                }


                if (dbVersion >= -1 && dbVersion < appVersion) {//db版本為-1或有紀錄時並且db版本小於app 版本,則更新指定的table並嚐試遷移該table紀錄
                    insertIntoSql.setFromTableName(tableName + "__temp");
                    updateTableAndMigrateDataWithInsertIntoSql(connection, tableName, tableName, createSql, insertIntoSql.getInsertIntoSQL());
                    isUpdated = true;
                }

            }
        }


        return isUpdated;
    }

    public void updateTableAndMigrateDataWithInsertIntoSql(
            Connection connection, String existingTableName, String updatedTableName, String
            sql, String insertIntoSql) {

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("");

            //移除殘留的temp的table
            String sqlTemp = "DROP TABLE IF EXISTS " + existingTableName + "__temp";
            preparedStatement.execute(sqlTemp);

            //將原有的table 改名為tableName+"__temp"
            sqlTemp = "ALTER TABLE " + existingTableName + " RENAME TO " + existingTableName + "__temp";
            preparedStatement.execute(sqlTemp);


            //執行使用者需求的sql 語句
            preparedStatement.execute(sql);

            //將temp的table 資料賦給updated的table
            if (insertIntoSql != null) {
                sqlTemp = insertIntoSql;
            } else {
                sqlTemp = "INSERT INTO " + updatedTableName + " SELECT * FROM " + existingTableName + "__temp";
            }
            preparedStatement.execute(sqlTemp);


            //移除temp的table
            sqlTemp = "DROP TABLE " + existingTableName + "__temp";
            boolean execute = preparedStatement.execute(sqlTemp);


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getConstructionSql() {
        return constructionSql;
    }

    public void setConstructionSql(String constructionSql) {
        this.constructionSql = constructionSql;
    }

    public String getCurrentColumns() {
        return currentColumns;
    }

    public void setCurrentColumns(String currentColumns) {
        this.currentColumns = currentColumns;
    }

    public String getSelectedColumns() {
        return selectedColumns;
    }

    public void setSelectedColumns(String selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

}
