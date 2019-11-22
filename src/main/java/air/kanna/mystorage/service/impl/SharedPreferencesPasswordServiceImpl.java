package air.kanna.mystorage.service.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import air.kanna.mystorage.MyStorage;
import air.kanna.mystorage.service.PasswordService;
import air.kanna.mystorage.util.NumberUtil;
import air.kanna.mystorage.util.StringUtil;

/**
 * Created by luo.yaohui on 2018/3/13.
 */

public class SharedPreferencesPasswordServiceImpl implements PasswordService {
    private Context context;
    private SharedPreferences passConfig;

    public SharedPreferencesPasswordServiceImpl(Context context){
        if(context == null){
            throw new NullPointerException("Context is null");
        }
        this.context = context;

        initService();
    }
    @Override
    public boolean createPassword(String newPassword) {
        if(StringUtil.isSpace(newPassword)){
            return false;
        }
        if(passConfig.contains(MyStorage.PASSWORD_ENCODED)){
            return false;
        }
        String encStr = null;
        try {
            encStr = getSHA256Hash(newPassword);
        }catch(Exception e){
            Log.e(MyStorage.LOG_TAG, "CreatePassword encode error.", e);
        }

        if(encStr == null || encStr.length() <= 0){
            return false;
        }
        SharedPreferences.Editor editor = passConfig.edit();
        editor.putString(MyStorage.PASSWORD_ENCODED, encStr);
        editor.commit();
        return true;
    }

    @Override
    public boolean resetPassword(String oldPassword, String newPassword, String comPassword) {
        if(StringUtil.isSpace(oldPassword)
                || StringUtil.isSpace(newPassword)
                || StringUtil.isSpace(comPassword)){
            return false;
        }
        if(newPassword.equalsIgnoreCase(oldPassword)){
            return false;
        }
        if(!newPassword.equals(comPassword)){
            return false;
        }
        if(!checkPassword(oldPassword)){
            return false;
        }
        String encStr = null;
        try {
            encStr = getSHA256Hash(newPassword);
        }catch(Exception e){
            Log.e(MyStorage.LOG_TAG, "CreatePassword encode error.", e);
        }

        if(encStr == null || encStr.length() <= 0){
            return false;
        }

        SharedPreferences.Editor editor = passConfig.edit();
        editor.putString(MyStorage.PASSWORD_ENCODED, encStr);
        editor.commit();
        return true;
    }

    @Override
    public boolean checkPassword(String inPassword) {
        if(!hasPassword()){
            return false;
        }
        if(StringUtil.isSpace(inPassword)){
            return false;
        }
        String encStr = null;
        try {
            encStr = getSHA256Hash(inPassword);
        }catch(Exception e){
            Log.e(MyStorage.LOG_TAG, "CreatePassword encode error.", e);
        }

        if(encStr == null || encStr.length() <= 0){
            return false;
        }
        String saved = passConfig.getString(MyStorage.PASSWORD_ENCODED, "");
        return saved.equalsIgnoreCase(encStr);
    }

    @Override
    public boolean hasPassword() {
        return passConfig.contains(MyStorage.PASSWORD_ENCODED);
    }

    private void initService(){
        passConfig = context.getSharedPreferences(
                MyStorage.APP_CONFIG_NAME, Context.MODE_PRIVATE);
    }

    private String getSHA256Hash(String org) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");//可以换成SHA-1、SHA-512、SHA-384等参数
        byte[] srcBytes = org.getBytes();

        md.update(srcBytes);

        return NumberUtil.toHexString(md.digest());
    }
}
