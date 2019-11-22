package air.kanna.mystorage.android;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

public class BasicActivity extends AppCompatActivity {
    protected static final int REQ_PERMISSION_STORAGE = 61531;
    protected static final int REQ_PERMISSION_CAMERA = 5626;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不显示标题栏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        //设置成竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityManager.addActivity(this);
    }

    @Override
    protected void onDestroy(){
        ActivityManager.removeActivity(this);

        super.onDestroy();
    }

    protected void requestPermission(String[] permissions, int requestCode){
        if(permissions != null && permissions.length > 0){
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }
}
