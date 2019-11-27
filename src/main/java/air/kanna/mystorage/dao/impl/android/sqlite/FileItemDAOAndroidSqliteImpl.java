package air.kanna.mystorage.dao.impl.android.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import air.kanna.mystorage.MyStorage;
import air.kanna.mystorage.dao.FileItemDAO;
import air.kanna.mystorage.dao.OrderBy;
import air.kanna.mystorage.dao.Pager;
import air.kanna.mystorage.dao.condition.FileItemCondition;
import air.kanna.mystorage.model.dto.FileItemDTO;
import air.kanna.mystorage.util.StringUtil;

public class FileItemDAOAndroidSqliteImpl
        extends BaseAndroidSqliteDAO<FileItemDTO>
        implements FileItemDAO {

    public FileItemDAOAndroidSqliteImpl(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public int listByConditionCount(FileItemCondition condition) {
        StringBuilder sql = new StringBuilder();
        List<String> param = new ArrayList<>();
        String conditionSql = getConditionSQL(condition, param);
        String[] array = param.toArray(new String[param.size()]);
        String fullSql = null;

        sql.append(" SELECT count(*) FROM ").append(getTableName()).append(" WHERE 1 = 1 ");
        sql.append(conditionSql);
        fullSql = sql.toString();

        Log.i(MyStorage.LOG_TAG, fullSql);

        Cursor result = database.rawQuery(fullSql, array);

        if(result == null || !result.moveToNext()) {
            return -1;
        }

        return result.getInt(0);
    }

    @Override
    public List<FileItemDTO> listByCondition(FileItemCondition condition, OrderBy order, Pager pager) {
        StringBuilder sql = new StringBuilder();
        List<String> param = new ArrayList<>();
        List<FileItemDTO> result = new ArrayList<>();
        String conditionSql = getConditionSQL(condition, param);
        String[] array = param.toArray(new String[param.size()]);
        String fullSql = null;

        sql.append(" SELECT * FROM ").append(getTableName()).append(" WHERE 1 = 1 ");
        sql.append(conditionSql);
        sql.append(getOrderSQL(order));
        sql.append(getPagerSQL(pager));
        fullSql = sql.toString();

        Log.i(MyStorage.LOG_TAG, fullSql);

        Cursor cursor = database.rawQuery(fullSql, array);
        if(cursor == null) {
            return result;
        }

        for(;cursor.moveToNext();) {
            FileItemDTO object = cursor2Object(cursor);
            if(object != null) {
                result.add(object);
            }
        }
        return result;
    }

    @Override
    protected FileItemDTO cursor2Object(Cursor cursor) {
        FileItemDTO item = new FileItemDTO();

        item.setId(cursor.getLong(cursor.getColumnIndex("id")));
        item.setDiskId(cursor.getLong(cursor.getColumnIndex("disk_id")));
        item.setFileName(cursor.getString(cursor.getColumnIndex("file_name")));
        item.setFileType((char)cursor.getInt(cursor.getColumnIndex("file_type")));
        item.setFileSize(cursor.getLong(cursor.getColumnIndex("file_size")));
        item.setFilePath(cursor.getString(cursor.getColumnIndex("file_path")));

        item.setFileHash01(cursor.getString(cursor.getColumnIndex("file_hash_01")));
        item.setFileHash02(cursor.getString(cursor.getColumnIndex("file_hash_02")));
        item.setFileHash03(cursor.getString(cursor.getColumnIndex("file_hash_03")));

        item.setCreateDate(cursor.getLong(cursor.getColumnIndex("create_date")));
        item.setLastModDate(cursor.getLong(cursor.getColumnIndex("update_date")));
        item.setRemark(cursor.getString(cursor.getColumnIndex("remark")));

        return item;
    }

    @Override
    protected String getTableName() {
        return "file_list";
    }

    @Override
    protected String getKeyCloumName() {
        return "id";
    }

    private String getConditionSQL(FileItemCondition condition, List<String> params) {
        StringBuilder sb = new StringBuilder();

        if(condition.getDiskId() != null && condition.getDiskId() > 0) {
            sb.append(" AND disk_id = ? ");
            params.add("" + condition.getDiskId());
        }
        if(StringUtil.isNotNull(condition.getFileName())) {
            sb.append(" AND file_name LIKE ? ");
            params.add('%' + condition.getFileName() + '%');
        }
        if(condition.getFileType() >= '0') {
            sb.append(" AND file_type = ? ");
            params.add("" + (int)condition.getFileType());
        }
        if(condition.getFileSizeMin() != null) {
            sb.append(" AND file_size >= ? ");
            params.add("" + condition.getFileSizeMin().longValue());
        }
        if(condition.getFileSizeMax() != null) {
            sb.append(" AND file_size <= ? ");
            params.add("" + condition.getFileSizeMax().longValue());
        }
        if(StringUtil.isNotNull(condition.getFilePath())) {
            sb.append(" AND file_path LIKE ? ");
            params.add('%' + condition.getFilePath() + '%');
        }

        if(condition.getCreateDateMin() != null) {
            sb.append(" AND create_date >= ? ");
            params.add("" + condition.getCreateDateMin().longValue());
        }
        if(condition.getCreateDateMax() != null) {
            sb.append(" AND create_date <= ? ");
            params.add("" + condition.getCreateDateMax().longValue());
        }

        if(condition.getLastModMin() != null) {
            sb.append(" AND update_date >= ? ");
            params.add("" + condition.getLastModMin().longValue());
        }
        if(condition.getLastModMax() != null) {
            sb.append(" AND update_date <= ? ");
            params.add("" + condition.getLastModMax().longValue());
        }

        return sb.toString();
    }
}
