package air.kanna.mystorage.service.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import air.kanna.mystorage.MyStorage;
import air.kanna.mystorage.config.BaseSharedPreferencesConfigService;
import air.kanna.mystorage.model.MyStorageConfig;
import air.kanna.mystorage.service.MyStorageConfigService;
import air.kanna.mystorage.util.StringUtil;

public class MyStorageConfigServiceSharedPreferencesImpl
        extends BaseSharedPreferencesConfigService<MyStorageConfig>
        implements MyStorageConfigService {

    public MyStorageConfigServiceSharedPreferencesImpl(Context context) {
        super(context);
    }

    @Override
    public MyStorageConfig getConfig() {
        if(passConfig == null) {
            return null;
        }
        MyStorageConfig config = new MyStorageConfig();
        
        String temp = passConfig.getString("selectedPath", "");
        if(StringUtil.isNotSpace(temp)) {
            config.setSelectedPath(temp);
        }

        temp = passConfig.getString("dbFileName", "");
        if(StringUtil.isNotSpace(temp)) {
            config.setDbFileName(temp);
        }
        
        
        temp = passConfig.getString("searchFileName", "");
        if(StringUtil.isNotSpace(temp)) {
            config.setSearchFileName(temp);
        }
        
        temp = passConfig.getString("searchFileType", "");
        if(StringUtil.isNotSpace(temp)) {
            config.setSearchFileType(temp);
        }
        
        temp =passConfig.getString("searchDiskPath", "");
        if(StringUtil.isNotSpace(temp)) {
            config.setSearchDiskPath(temp);
        }


        temp = passConfig.getString("pageSize", "");
        if(StringUtil.isNotSpace(temp)) {
            try {
                int pagesize = Integer.parseInt(temp);
                if(pagesize > 0) {
                    config.setPageSize(pagesize);
                }
            }catch(Exception e) {
                Log.w(MyStorage.LOG_TAG, "parse page size error", e);
            }
        }

        return config;
    }

    @Override
    public boolean saveConfig(MyStorageConfig config) {
        if(config == null || passConfig == null) {
            return false;
        }
        SharedPreferences.Editor editor = passConfig.edit();
        if(editor == null){
            return false;
        }

        editor.putString("selectedPath", config.getSelectedPath());
        editor.putString("dbFileName", "" + config.getDbFileName());
        
        editor.putString("searchFileName", config.getSearchFileName());
        editor.putString("searchFileType", config.getSearchFileType());
        editor.putString("searchDiskPath", config.getSearchDiskPath());

        editor.putString("pageSize", ("" + config.getPageSize()));

        return editor.commit();
    }

}
