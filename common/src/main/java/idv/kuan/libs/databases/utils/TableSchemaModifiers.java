package idv.kuan.libs.databases.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableSchemaModifiers {
    private static final String DB_VERSION_TABLE = "db_version_table";
    private static final String TABLE_COLUMN_DATABASE_VERSION = "database_version";

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

    private static void updateDBVersionTableStructure(Connection connection) {
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
     * 更新每個資料結構後,務必執行updateDBVersion
     *
     * @param connection
     * @param appVersion 取得方法:packageManager.getPackageInfo(getPackageName(), 0).versionCode
     * @param tableName
     * @param createSql
     * @return
     */
    public static boolean createOrUpdateTableWithDataMigration(Connection connection, int appVersion,
                                                               String tableName, String createSql, String partMigrateSql) {
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

                int dbVersion = getDBVersion(connection);

                if (dbVersion == -2) {//無紀錄時,則重建新的db version table 結構,和鍵入一筆值為-1的default 紀錄
                    updateDBVersionTableStructure(connection);
                    dbVersion = getDBVersion(connection);
                }


                if (dbVersion >= -1 && dbVersion < appVersion) {//db版本為-1或有紀錄時並且db版本小於app 版本,則更新指定的table並嚐試遷移該table紀錄
                    if (partMigrateSql == null) {
                        updateTableAndMigrateData(connection, tableName, tableName, createSql);

                    } else {
                        updateTableAndMigrateDataWithPartMigrateSql(connection, tableName, tableName, createSql, partMigrateSql);
                    }
                    isUpdated = true;
                }

            }
        }



        return isUpdated;
    }

    /**
     * 一般修改結構,不改欄位名,不減少欄位
     * 遷移全部紀錄
     * 建議使用updateTableAndMigrateDataWithPartMigrateSql
     *
     * @param connection
     * @param existingTableName
     * @param updatedTableName
     * @param sql
     */
    public static void updateTableAndMigrateData(Connection connection, String existingTableName, String updatedTableName, String sql) {
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
            sqlTemp = "INSERT INTO " + updatedTableName + " SELECT * FROM " + existingTableName + "__temp";
            preparedStatement.execute(sqlTemp);

            //移除temp的table
            sqlTemp = "DROP TABLE " + existingTableName + "__temp";
            boolean execute = preparedStatement.execute(sqlTemp);


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * 可修改欄位名,增減欄位
     * 遷移紀錄由partMigrateSql和其組成的語句決定
     * @param connection
     * @param existingTableName
     * @param updatedTableName
     * @param sql
     * @param partMigrateSql
     */
    public static void updateTableAndMigrateDataWithPartMigrateSql(Connection connection, String existingTableName,
                                                                   String updatedTableName, String sql, String partMigrateSql) {


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
            sqlTemp = "INSERT INTO " + updatedTableName + partMigrateSql + " FROM " + existingTableName + "__temp";
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
