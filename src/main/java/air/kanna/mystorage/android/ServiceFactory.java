package air.kanna.mystorage.android;

import air.kanna.mystorage.service.MyStorageConfigService;
import air.kanna.mystorage.service.PasswordService;

/**
 * Created by lenovo on 2019-11-25.
 */

public class ServiceFactory {
    private static MyStorageConfigService configService = null;
    private static PasswordService passwordService = null;

    public static MyStorageConfigService getConfigService(){
        return configService;
    }

    public static void registeredConfigService(MyStorageConfigService config){
        if(config == null){
            throw new NullPointerException("MyStorageConfigService is null");
        }
        configService = config;
    }

    public static PasswordService getPasswordService() {
        return passwordService;
    }

    public static void registeredPasswordService(PasswordService password) {
        if(password == null){
            throw new NullPointerException("PasswordService is null");
        }
        passwordService = password;
    }
}
