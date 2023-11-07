package idv.kuan.libs.databases.modifier;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLiteSchemaModifierUtil {
    public static final String DB_VERSION_TABLE = "db_version_table";
    public static final String TABLE_COLUMN_DATABASE_VERSION = "database_version";


    public static class ColumnsMappingSql {
        private String tableName;
        private String insertIntoSql;
        private String fromTableName;
        private String columns;
        private String selectedColumns;

        public ColumnsMappingSql(String tableName) {
            this.tableName = tableName;
        }

        public ColumnsMappingSql(String tableName, String fromTableName) {
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


    private SQLiteSchemaModifierUtil() {

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

    public static void initializeDatabaseVersionTable(Connection connection) {
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
