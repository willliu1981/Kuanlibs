package idv.kuan.libs.databases.daos;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import idv.kuan.libs.databases.DBFactoryCreator;
import idv.kuan.libs.databases.models.CommonEntity;
import idv.kuan.libs.databases.utils.QueryBuilder;

public abstract class CommonDao<V extends CommonEntity> implements Dao<V> {

    protected abstract V createNewEntity();

    @Override
    public <U> U findByIDOrAll(V entity) throws SQLException {
        if (entity == null) {
            throw new SQLException("entity is null");
        }
        Connection connection = DBFactoryCreator.getFactory().getConnection();
        String sqlQuery = "select * from " + getTableName();
        PreparedStatement preparedStatement = null;
        if (entity.getId() == null) {
            preparedStatement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<V> list = new ArrayList<>();
            while (resultSet.next()) {
                entity = createNewEntity();
                mapResultSetToEntity(entity, resultSet);
                list.add(entity);
            }

            return (U) list;

        } else {
            preparedStatement = connection.prepareStatement(sqlQuery + " where id=?");
            preparedStatement.setInt(1, entity.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            entity = createNewEntity();
            if (resultSet.next()) {
                mapResultSetToEntity(entity, resultSet);
            }
            return (U) entity;
        }
    }

    @Override
    public V findById(V entity) throws SQLException {
        return findByIDOrAll(entity);
    }

    @Override
    public List<V> findAll() throws SQLException {
        return findByIDOrAll(createNewEntity());
    }

    /**
     * 注意:connection 是從DBFactoryCreator取得預設factory的connection
     *
     * @param entity
     * @throws SQLException
     */
    @Override
    public void createOrUpdateEntity(V entity) throws SQLException {
        if (entity == null) {
            throw new SQLException("entity is null");
        }

        Connection connection = DBFactoryCreator.getFactory().getConnection();

        QueryBuilder builder = new QueryBuilder();
        populateBuilderWithEntityProperties(builder, entity);

        String query = null;

        if (entity.getId() == null) {
            query = builder.buildInsertQuery(getTableName());
        } else {
            String condition = "id = " + entity.getId();
            query = builder.buildUpdateQuery(getTableName(), condition);
        }


        PreparedStatement statment = connection.prepareStatement(query);
        builder.prepareStatement(statment);
        statment.executeUpdate();


    }


    /**
     * 會執行createOrUpdateEntity方法
     *
     * @param entity
     * @throws SQLException
     */
    @Override
    public void create(V entity) throws SQLException {
        this.createOrUpdateEntity(entity);
    }

    /**
     * 會執行createOrUpdateEntity方法
     *
     * @param entity
     * @throws SQLException
     */
    @Override
    public void update(V entity) throws SQLException {
        this.createOrUpdateEntity(entity);
    }


    @Override
    public void delete(V entity) throws SQLException {
        if (entity == null) {
            throw new SQLException("entity is null");
        }

        Connection connection = DBFactoryCreator.getFactory().getConnection();
        String sql = "delete from " + getTableName() + " where id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, entity.getId());
        boolean execute = preparedStatement.execute();
        System.out.println("dbg CD:delete resulte is " + execute);
    }

    /**
     * 使用createOrUpdateEntity方法時,必需實作的方法
     * 注意,如果create是AI,不應填id; update 不變更id,不應填id
     *
     * @param builder
     * @param entity
     */
    protected abstract void populateBuilderWithEntityProperties(QueryBuilder builder, V entity);

    protected abstract void mapResultSetToEntity(V entity, ResultSet resultSet) throws SQLException;

    protected abstract String getTableName();

}
