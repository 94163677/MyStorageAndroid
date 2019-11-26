package air.kanna.mystorage.config;

public interface ConfigService<T> {
    T getConfig();
    boolean saveConfig(T config);
}
