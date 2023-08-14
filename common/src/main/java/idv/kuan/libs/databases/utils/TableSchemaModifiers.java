package idv.kuan.libs.databases.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableSchemaModifiers {
    private static final String DBVersionTable = "db_version_table";
    private static final String TableColumnDatabase_version = "database_version";

    public static void updateDBVersion(Connection connection, int databaseVersion) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "update " + DBVersionTable + " set " + TableColumnDatabase_version + "=?");
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
                    "select " + TableColumnDatabase_version + " from " + DBVersionTable);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int database_version = resultSet.getInt(TableColumnDatabase_version);
                if (database_version < -1) {
                    throw new SQLException(TableColumnDatabase_version + " 應該回傳大於等於-1");
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
        String dropDBVersionTableSql = "DROP TABLE " + DBVersionTable;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(dropDBVersionTableSql);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String DBVersionTableCreateSql = "CREATE TABLE \"" + DBVersionTable + "\" ( " +
                " \"" + TableColumnDatabase_version + "\" INTEGER DEFAULT -1 " +
                ")";
        createNew(connection, DBVersionTableCreateSql);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("insert into " + DBVersionTable + " values(-1)");
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
            if (!isTableExist) {
                createNew(connection, createSql);
            } else {
                Boolean isDBVersionTableExist = isTableExist(connection, DBVersionTable);
                if (!isDBVersionTableExist) {
                    String DBVersionTableCreateSql = "CREATE TABLE \"" + DBVersionTable + "\" ( " +
                            " \"" + TableColumnDatabase_version + "\" INTEGER DEFAULT -1 " +
                            ")";
                    createNew(connection, DBVersionTableCreateSql);

                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement("insert into " + DBVersionTable + " values(-1)");
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                int dbVersion = getDBVersion(connection);

                if (dbVersion == -2) {
                    updateDBVersionTableStructure(connection);
                    dbVersion = getDBVersion(connection);
                }


                if (dbVersion >= -1 && dbVersion < appVersion) {
                    if (partMigrateSql == null) {
                        updateTableAndMigrateData(connection, tableName, tableName, createSql);

                    } else {
                        updateTableAndMigrateData2(connection, tableName, tableName, createSql, partMigrateSql);
                    }
                    isUpdated = true;
                }

            }
        }


        System.out.println("xxx TSMs test b");
        return isUpdated;
    }

    /**
     * 一般修改結構,不改欄位名,不減少欄位
     * 建議使用createNewOrEvolveTableStructure
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


    public static void updateTableAndMigrateData2(Connection connection, String existingTableName,
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
