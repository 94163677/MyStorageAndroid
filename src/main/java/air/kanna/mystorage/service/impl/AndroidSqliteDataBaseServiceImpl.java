package air.kanna.mystorage.service.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import air.kanna.mystorage.service.DataBaseService;
import kanna.air.mystorage.android.R;

public class AndroidSqliteDataBaseServiceImpl implements DataBaseService<SQLiteDatabase> {
    private Context context;

    public AndroidSqliteDataBaseServiceImpl(Context context){
        if(context == null){
            throw new NullPointerException("Context is null");
        }
        this.context = context;
    }

    @Override
    public String checkDataBase(SQLiteDatabase dataBase) {
        String baseMsg = context.getString(R.string.dbfile_check_error);
        StringBuilder sb = new StringBuilder();

        try {
            int count = -1;
            Cursor cursor = dataBase.rawQuery("SELECT COUNT(*) FROM db_version", new String[]{});
            if(cursor == null || !cursor.moveToNext()){
                sb.append(context.getString(R.string.table_version_error));
            }
            count = cursor.getInt(0);
            if(count <= 0){
                sb.append(context.getString(R.string.table_version_error));
            }

            cursor = dataBase.rawQuery("SELECT COUNT(*) FROM disk_description", new String[]{});
            if(cursor == null || !cursor.moveToNext()){
                sb.append(context.getString(R.string.table_disk_error));
            }
            count = cursor.getInt(0);

            cursor = dataBase.rawQuery("SELECT COUNT(*) FROM file_list", new String[]{});
            if(cursor == null || !cursor.moveToNext()){
                sb.append(context.getString(R.string.table_file_error));
            }
            count = cursor.getInt(0);
        }catch (Exception e){
            sb.append(e.getMessage());
        }

        if(sb.length() > 0){
            sb.insert(0, baseMsg);
            return sb.toString();
        }
        return null;
    }
}
