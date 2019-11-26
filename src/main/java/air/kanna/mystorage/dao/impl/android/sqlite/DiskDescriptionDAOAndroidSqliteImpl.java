package air.kanna.mystorage.dao.impl.android.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import air.kanna.mystorage.dao.DiskDescriptionDAO;
import air.kanna.mystorage.dao.OrderBy;
import air.kanna.mystorage.dao.Pager;
import air.kanna.mystorage.model.dto.DiskDescriptionDTO;
import air.kanna.mystorage.util.StringUtil;

public class DiskDescriptionDAOAndroidSqliteImpl
        extends BaseAndroidSqliteDAO<DiskDescriptionDTO> implements DiskDescriptionDAO {

    public DiskDescriptionDAOAndroidSqliteImpl(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public List<DiskDescriptionDTO> listByCondition(String basePath, String desc, OrderBy order, Pager pager) {
        List<String> param = new ArrayList<>();
        List<DiskDescriptionDTO> result = new ArrayList<>();
        String sql = getConditionSQL(basePath, desc, param, order, pager);
        String[] array = param.toArray(new String[param.size()]);

        Cursor cursor = database.rawQuery(sql, array);

        if(cursor == null) {
            return result;
        }
        for(;cursor.moveToNext();) {
            DiskDescriptionDTO object = cursor2Object(cursor);
            if(object != null) {
                result.add(object);
            }
        }
        return result;
    }

    private String getConditionSQL(
            String basePath, String desc, List<String> params, OrderBy order, Pager pager) {
        StringBuilder sb = new StringBuilder();
        params.clear();

        sb.append(" SELECT * FROM ").append(getTableName()).append(" WHERE 1 = 1 ");

        if(StringUtil.isNotNull(basePath)) {
            sb.append(" AND base_path LIKE ? ");
            params.add('%' + basePath + '%');
        }
        if(StringUtil.isNotNull(desc)) {
            sb.append(" AND description LIKE ? ");
            params.add('%' + desc + '%');
        }
        sb.append(getOrderSQL(order));
        sb.append(getPagerSQL(pager));
        return sb.toString();
    }

    @Override
    protected DiskDescriptionDTO cursor2Object(Cursor cursor) {
        if(cursor == null){
            return null;
        }
        DiskDescriptionDTO disk = new DiskDescriptionDTO();

        disk.setId(cursor.getLong(cursor.getColumnIndex("id")));
        disk.setVersion(cursor.getString(cursor.getColumnIndex("version")));
        disk.setBasePath(cursor.getString(cursor.getColumnIndex("base_path")));
        disk.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        disk.setLastUpdate(cursor.getString(cursor.getColumnIndex("last_update")));

        return disk;
    }

    @Override
    protected String getTableName() {
        return "disk_description";
    }

    @Override
    protected String getKeyCloumName() {
        return "id";
    }
}
