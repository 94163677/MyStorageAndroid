package air.kanna.mystorage.dao.impl.sqlite;

import android.util.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import air.kanna.mystorage.dao.BaseModelDAO;
import air.kanna.mystorage.dao.OrderBy;
import air.kanna.mystorage.dao.Pager;

public abstract class BaseSqliteDAO<T> implements BaseModelDAO<T>{
    protected Connection conn;
    protected Statement stat;
    
    public BaseSqliteDAO(Connection conn) throws SQLException{
        if(conn == null) {
            throw new NullPointerException("Connection is null");
        }
        if(conn.isClosed()) {
            throw new SQLException("Connection is closed");
        }
        if(conn.isReadOnly()) {
            throw new SQLException("Connection is ReadOnly");
        }
        this.conn = conn;
        this.stat = conn.createStatement();
    }
    
    protected abstract String getTableName();
    protected abstract String getKeyCloumName();
    protected abstract String getInsertSQL(T object);
    protected abstract String getUpdateSQL(T object);
    protected abstract T resultSet2Object(ResultSet result)throws SQLException;
    
    @Override
    public T getById(Object id) {
        ResultSet result = getByIdResultSet(id);
        if(result == null) {
            return null;
        }
        try {
            if(!result.next()) {
                Log.w(this.getClass().getName(), "Cannot found Object by id: " + id.toString());
                return null;
            }
            
            return resultSet2Object(result);
        }catch(SQLException e) {
            Log.e(this.getClass().getName(), "Parse ResultSet error", e);
        }
        return null;
    }
    
    @Override
    public int update(T object) {
        if(object == null) {
            Log.w(this.getClass().getName(), "DiskDescriptionDTO is null");
            return -1;
        }
        String sql = getUpdateSQL(object);
        
        try {
            return stat.executeUpdate(sql);
        }catch(SQLException e) {
            Log.e(this.getClass().getName(), "Update object error: " + sql, e);
        }
        return -1;
    }
    
    @Override
    public List<T> listAll(OrderBy order, Pager pager){
        ResultSet result = listAllResultSet(order, pager);
        List<T> list = new ArrayList<>();
        
        if(result == null) {
            return null;
        }
        try {
            for(;result.next();) {
                T object = resultSet2Object(result);
                if(object != null) {
                    list.add(object);
                }
            }
        }catch(SQLException e) {
            Log.e(this.getClass().getName(), "Parse ResultSet error", e);
            return null;
        }
        return list;
    }
    
    @Override
    public int insert(T object) {
        if(object == null) {
            Log.w(this.getClass().getName(), "insert object is null");
            return -1;
        }
        String sql = getInsertSQL(object);

        try {
            return stat.executeUpdate(sql);
        }catch(SQLException e) {
            Log.e(this.getClass().getName(), "Insert object error: " + sql, e);
        }
        return -1;
    }
    
    @Override
    public int deleteById(Object id) {
        if(id == null) {
            Log.w(this.getClass().getName(), "Object's id is null");
            return -1;
        }
        
        StringBuilder sb = new StringBuilder();

        sb.append("DELETE FROM ").append(getTableName())
            .append(" WHERE ").append(getKeyCloumName())
            .append(" = ");
        
        if(id instanceof Number) {
            sb.append(id.toString());
        }else {
            sb.append('\'').append(id.toString()).append('\'');
        }
        
        String sql = sb.toString();
        
        try {
            return stat.executeUpdate(sql);
        }catch(SQLException e) {
            Log.e(this.getClass().getName(), "delete object error: " + sql, e);
            return -1;
        }
    }
    
    protected ResultSet getByIdResultSet(Object id) {
        if(id == null) {
            Log.w(this.getClass().getName(), "Object's id is null");
            return null;
        }
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ").append(getTableName())
            .append(" WHERE ").append(getKeyCloumName())
            .append(" = ");
        
        if(id instanceof Number) {
            sb.append(id.toString());
        }else {
            sb.append('\'').append(id.toString()).append('\'');
        }

        Log.i(this.getClass().getName(), sb.toString());
        try {
            return stat.executeQuery(sb.toString());
        }catch(SQLException e) {
            Log.e(this.getClass().getName(), "select error: " + sb.toString(), e);
            return null;
        }
    }
    
    protected ResultSet listAllResultSet(OrderBy order, Pager pager) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ").append(getTableName());
        sb.append(getOrderSQL(order));
        sb.append(getPagerSQL(pager));
        
        Log.i(this.getClass().getName(), sb.toString());
        try {
            return stat.executeQuery(sb.toString());
        }catch(SQLException e) {
            Log.e(this.getClass().getName(), "list all error: " + sb.toString(), e);
            return null;
        }
    }
    
    protected String getOrderSQL(OrderBy order) {
        if(order != null && order.getOrderPairs().size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(" ORDER BY ");
            for(String[] pair : order.getOrderPairs()) {
                sb.append(pair[1]).append(' ').append(pair[0]).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return "";
    }
    
    protected String getPagerSQL(Pager pager) {
        if(pager != null) {
            StringBuilder sb = new StringBuilder();
            int offset = (pager.getPage() - 1) * pager.getSize();
            
            sb.append(" LIMIT ").append(pager.getSize());
            sb.append(" OFFSET ").append(offset);
            
            return sb.toString();
        }
        return "";
    }
}
