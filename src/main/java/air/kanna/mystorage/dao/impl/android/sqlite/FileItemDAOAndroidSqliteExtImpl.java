package air.kanna.mystorage.dao.impl.android.sqlite;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import air.kanna.mystorage.dao.condition.FileItemCondition;
import air.kanna.mystorage.dao.condition.FileItemConditionExt;
import air.kanna.mystorage.util.StringUtil;

public class FileItemDAOAndroidSqliteExtImpl extends FileItemDAOAndroidSqliteImpl{
    public FileItemDAOAndroidSqliteExtImpl(SQLiteDatabase database){
        super(database);
    }

    @Override
    protected String getConditionSQL(FileItemCondition condition, List<String> params) {
        if(condition instanceof FileItemConditionExt) {
            return getConditionSQLExt((FileItemConditionExt)condition, params);
        }
        return super.getConditionSQL(condition, params);
    }
    
    private String getConditionSQLExt(FileItemConditionExt ext, List<String> params) {
        StringBuilder sb = new StringBuilder();

        if(ext.getDiskId() != null && ext.getDiskId() > 0) {
            sb.append(" AND disk_id = ? ");
            params.add("" + ext.getDiskId());
        }
        
        List<String> nameCnd = checkAndGetUnSpace(ext.getFileNameIncludeAll());
        if(nameCnd.size() > 0) {
            for(String incAll : nameCnd) {
                sb.append(" AND file_name LIKE ? ");
                params.add(new StringBuilder().append('%').append(incAll).append('%').toString());
            }
        }
        
        nameCnd = checkAndGetUnSpace(ext.getFileNameIncludeOne());
        if(nameCnd.size() > 0) {
            sb.append(" AND ( ");
            for(int i=0; i<nameCnd.size(); i++) {
                String incAll = nameCnd.get(i);
                if(i == 0) {
                    sb.append(" file_name LIKE ? ");
                }else {
                    sb.append(" OR file_name LIKE ? ");
                }
                params.add(new StringBuilder().append('%').append(incAll).append('%').toString());
            }
            sb.append(" ) ");
        }
        
        nameCnd = checkAndGetUnSpace(ext.getFileNameExclude());
        if(nameCnd.size() > 0) {
            for(String incAll : nameCnd) {
                sb.append(" AND file_name NOT LIKE ? ");
                params.add(new StringBuilder().append('%').append(incAll).append('%').toString());
            }
        }
        
        sb.append(getNormalConditionSQL(ext, params));
        return sb.toString();
    }
    
    private List<String> checkAndGetUnSpace(List<String> orgList){
        if(orgList == null || orgList.size() <= 0) {
            return new ArrayList<>(1);
        }
        List<String> checked = new ArrayList<>(orgList.size());
        for(String checkStr : orgList) {
            if(StringUtil.isSpace(checkStr)) {
                continue;
            }
            checked.add(checkStr);
        }
        return checked;
    }
    
}
