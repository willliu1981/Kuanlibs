package idv.kuan.libs.databases.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLiteSchemaModifiers {
    private static final String DB_VERSION_TABLE = "db_version_table";
    private static final String TABLE_COLUMN_DATABASE_VERSION = "database_version";


    public static class SchemaModifier {
        private List<SchemaModifierExecutor> list = new ArrayList<>();
        private Connection connection;
        private int appVersion;

        private SchemaModifier(Connection connection, int appVersion) {
            this.connection = connection;
            this.appVersion = appVersion;
        }

        /**
         * @param schemaModifierExecutor 傳入connection, appVersion
         */
        public void addSchemaModifierExecutor(SchemaModifierExecutor schemaModifierExecutor) {
            this.list.add(schemaModifierExecutor);
        }

        public void createDatabase() {
            for (SchemaModifierExecutor se : list) {
                se.execute(connection, appVersion);
            }

            //最後必做update database version
            SQLiteSchemaModifiers.updateDBVersion(connection, appVersion);
        }

    }

    public static class SchemaModifierSQL {
        private String tableName;
        private String insertIntoSql;
        private String fromTableName;
        private String columns;
        private String selectedColumns;

        public SchemaModifierSQL(String tableName) {
            this.tableName = tableName;
        }

        public SchemaModifierSQL(String tableName, String fromTableName) {
            this(tableName);
            this.fromTableName = fromTableName;
        }

        public void setFromTableName(String tableName) {
            this.fromTableName = tableName;
        }

        public void createInsertIntoSQL(String columns, String selectedColumns) {
            this.columns = columns;
            this.selectedColumns = selectedColumns;

        }

        public String getInsertIntoSQL() {
            if (tableName == null || fromTableName == null) {
                return null;
            }

            return this.insertIntoSql = "INSERT INTO " + tableName + " (" + columns + ") select " + selectedColumns + " from " + fromTableName;

        }

    }


    private SQLiteSchemaModifiers() {

    }

    static public SchemaModifier createSchemaModifier(Connection connection, int appVersion) {


        return new SchemaModifier(connection, appVersion);
    }


    public static void updateDBVersion(Connection connection, int databaseVersion) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "update " + DB_VERSION_TABLE + " set " + TABLE_COLUMN_DATABASE_VERSION + "=?");
            preparedStatement.setInt(1, databaseVersion);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param connection
     * @return -2=查無結果; -3=異常; -4=編譯時的預設異常
     * -1 留給table 做為異常回傳值
     */
    public static int getDBVersion(Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "select " + TABLE_COLUMN_DATABASE_VERSION + " from " + DB_VERSION_TABLE);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int database_version = resultSet.getInt(TABLE_COLUMN_DATABASE_VERSION);
                if (database_version < -1) {
                    throw new SQLException(TABLE_COLUMN_DATABASE_VERSION + " 應該回傳大於等於-1");
                }
                return database_version;
            } else {
                return -2;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -3;
    }

    private static void initializeDatabaseVersionTable(Connection connection) {
        String dropDBVersionTableSql = "DROP TABLE " + DB_VERSION_TABLE;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(dropDBVersionTableSql);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String DBVersionTableCreateSql = "CREATE TABLE \"" + DB_VERSION_TABLE + "\" ( " +
                " \"" + TABLE_COLUMN_DATABASE_VERSION + "\" INTEGER DEFAULT -1 " +
                ")";
        createNew(connection, DBVersionTableCreateSql);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("insert into " + DB_VERSION_TABLE + " values(-1)");
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param connection
     * @param appVersion
     * @param tableName
     * @param createSql
     * @param partMigrateSqlMapping 以冒號 ":" 分隔前面和後面的column語句,前為目前要使用的columns,後為select 的 columns
     * @return
     */
    public static boolean createOrUpdateTableWithDataMigration(
            Connection connection, int appVersion, String tableName, String createSql, String partMigrateSqlMapping) {

        String[] split = partMigrateSqlMapping.split(":");

        SchemaModifierSQL schemaModifierSQL = new SchemaModifierSQL(tableName);
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
    public static boolean createOrUpdateTableWithDataMigration(
            Connection connection, int appVersion, String tableName, String createSql, SchemaModifierSQL insertIntoSql) {

        Boolean isTableExist = isTableExist(connection, tableName);
        boolean isUpdated = false;
        if (isTableExist != null) {
            if (!isTableExist) {//不存在table,創建新的
                createNew(connection, createSql);
            } else {//有同名table,則開始覆寫
                //DB版本table 是否存在
                Boolean isDBVersionTableExist = isTableExist(connection, DB_VERSION_TABLE);
                if (!isDBVersionTableExist) {//DB版本table 不存在則創建新的DB版本table
                    String DBVersionTableCreateSql = "CREATE TABLE \"" + DB_VERSION_TABLE + "\" ( " +
                            " \"" + TABLE_COLUMN_DATABASE_VERSION + "\" INTEGER DEFAULT -1 )";
                    createNew(connection, DBVersionTableCreateSql);

                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement("insert into " + DB_VERSION_TABLE + " values(-1)");
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                int dbVersion = getDBVersion(connection);

                if (dbVersion == -2) {//無紀錄時,則重建新的db version table 結構,和鍵入一筆值為-1的default 紀錄
                    initializeDatabaseVersionTable(connection);
                    dbVersion = getDBVersion(connection);
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


    public static void updateTableAndMigrateDataWithInsertIntoSql(
            Connection connection, String existingTableName, String updatedTableName, String sql, String insertIntoSql) {

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

    public static Boolean isTableExist(Connection connection, String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String[] types = {"TABLE"};
            ResultSet resultSet = metaData.getTables(null, null, tableName, types);

            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createNew(Connection connection, String sql) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
