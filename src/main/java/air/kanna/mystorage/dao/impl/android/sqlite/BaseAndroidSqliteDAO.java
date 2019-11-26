package air.kanna.mystorage.dao.impl.android.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import air.kanna.mystorage.MyStorage;
import air.kanna.mystorage.dao.BaseModelDAO;
import air.kanna.mystorage.dao.OrderBy;
import air.kanna.mystorage.dao.Pager;

public abstract class BaseAndroidSqliteDAO<T> implements BaseModelDAO<T> {
    protected SQLiteDatabase database;

    protected BaseAndroidSqliteDAO(SQLiteDatabase database){
        if(database == null){
            throw new NullPointerException("SQLiteDatabase is null");
        }
        if(!database.isOpen()){
            throw new IllegalArgumentException("SQLiteDatabase is closed");
        }
        this.database = database;
    }

    protected abstract String getTableName();
    protected abstract String getKeyCloumName();
    protected abstract T cursor2Object(Cursor cursor);

    public T getById(Object id){
        Cursor result = getKeyCursor(id);
        if(result == null) {
            return null;
        }
        if(!result.moveToNext()) {
            Log.w(MyStorage.LOG_TAG, "Cannot found Object by id: " + id.toString());
            return null;
        }

        return cursor2Object(result);
    }

    public List<T> listAll(OrderBy order, Pager pager){
        Cursor result = getAllCursor(order, pager);
        List<T> list = new ArrayList<>();

        if(result == null) {
            return null;
        }
        for(;result.moveToNext();) {
            T object = cursor2Object(result);
            if(object != null) {
                list.add(object);
            }
        }
        return list;
    }

    private Cursor getKeyCursor(Object key){
        if(key == null) {
            Log.w(MyStorage.LOG_TAG, "Object's id is null");
            return null;
        }
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ").append(getTableName())
                .append(" WHERE ").append(getKeyCloumName())
                .append(" = ? ");
        Log.i(MyStorage.LOG_TAG, sb.toString());

        return database.rawQuery(sb.toString(), new String[]{"" + key});
    }

    private Cursor getAllCursor(OrderBy order, Pager pager){
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ").append(getTableName());
        sb.append(getOrderSQL(order));
        sb.append(getPagerSQL(pager));

        return database.rawQuery(sb.toString(), new String[]{});
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
