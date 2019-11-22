package air.kanna.mystorage.service;

/**
 * Created by luo.yaohui on 2018/3/13.
 */

public interface PasswordService {
    public boolean createPassword(String newPassword);
    public boolean resetPassword(String oldPassword, String newPassword, String comPassword);

    public boolean checkPassword(String inPassword);
    public boolean hasPassword();
}
