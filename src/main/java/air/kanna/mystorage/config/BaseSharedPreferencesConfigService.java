package air.kanna.mystorage.config;

import android.content.Context;
import android.content.SharedPreferences;

import air.kanna.mystorage.MyStorage;

public abstract class BaseSharedPreferencesConfigService<T> implements ConfigService<T> {
    private Context context;
    protected SharedPreferences passConfig;
    
    public BaseSharedPreferencesConfigService(Context context) {
        if(context == null) {
            throw new NullPointerException("Context is null");
        }
        passConfig = context.getSharedPreferences(
                MyStorage.APP_CONFIG_NAME, Context.MODE_PRIVATE);
    }
}
