package air.kanna.mystorage.android;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import kanna.air.mystorage.android.R;

public class BasicActivity extends AppCompatActivity {
    protected static final int REQ_PERMISSION_STORAGE = 61531;
    protected static final int REQ_PERMISSION_CAMERA = 5626;

    protected Activity current;
    protected long prevTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不显示标题栏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        //设置成竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityManager.addActivity(this);

        current = this;
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

    protected void showInformationMessage(int id, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(current)
                .setTitle(R.string.dialog_title_infor)
                .setMessage(id)
                .setPositiveButton(R.string.ok_button, listener)
                .show();
    }

    protected void showInformationMessage(String msg, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(current)
                .setTitle(R.string.dialog_title_infor)
                .setMessage(msg)
                .setPositiveButton(R.string.ok_button, listener)
                .show();
    }

    protected void showErrorMessage(int id, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(current)
                .setTitle(R.string.dialog_title_error)
                .setMessage(id)
                .setPositiveButton(R.string.ok_button, listener)
                .show();
    }

    protected void showErrorMessage(String msg, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(current)
                .setTitle(R.string.dialog_title_error)
                .setMessage(msg)
                .setPositiveButton(R.string.ok_button, listener)
                .show();
    }
}
