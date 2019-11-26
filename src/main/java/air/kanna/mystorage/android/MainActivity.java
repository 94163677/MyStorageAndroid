package air.kanna.mystorage.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;

import air.kanna.mystorage.MyStorage;
import kanna.air.mystorage.android.R;

public class MainActivity extends BasicActivity {
    private static final int REQ_PERMISSION_STORAGE = 61531;
    private static final int REQ_PERMISSION_CAMERA = 5626;

    private EditText result;
    private Button scan;
    private Button list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = (EditText)findViewById(R.id.editText);
        scan = (Button)findViewById(R.id.button);
        list = findViewById(R.id.button2);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartScan();
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();

                intent.setClass(current, FileSearchActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_view_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String resultStr = scanResult.getContents();
            result.setText(resultStr);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQ_PERMISSION_STORAGE: {
                for(int i=0; i<grantResults.length; i++){
                    if(PackageManager.PERMISSION_DENIED == grantResults[i]){
                        ;//TODO 弹出框，之后退出app
                    }
                }
            };break;
            case REQ_PERMISSION_CAMERA:{
                doStartScan();
            };break;
            default: Log.w(MyStorage.LOG_TAG, "Cannot support requestCode: " + requestCode);
        }
    }

    private void onStartScan(){
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.VIBRATE
        };
        for(int i=0; i<permissions.length; i++){
            if(ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                requestPermission(permissions, REQ_PERMISSION_CAMERA);
                return;
            }
        }
        doStartScan();
    }

    private void doStartScan(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
//        integrator.setRequestCode(REQ_QRCODE);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    private void test(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyStorage/MyStorage.db";
        File path1 = Environment.getExternalStorageDirectory();

        File[] list = getExternalFilesDirs(Environment.MEDIA_MOUNTED);
        for(int i=0; list != null && i<list.length; i++){
            Log.i(MyStorage.LOG_TAG, list[i].getAbsolutePath());
        }
        list = path1.listFiles();
        for(int i=0; list != null && i<list.length; i++){
            Log.i(MyStorage.LOG_TAG, list[i].getAbsolutePath());
        }
        File db = new File(path);

        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM db_version", new String[]{});
            if(cursor != null && cursor.moveToNext()){
                Log.i(MyStorage.LOG_TAG, cursor.getString(0));
            }
            cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM disk_description", new String[]{});
            if(cursor != null && cursor.moveToNext()){
                Log.i(MyStorage.LOG_TAG, cursor.getString(0));
            }
            cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM file_list", new String[]{});
            if(cursor != null && cursor.moveToNext()){
                Log.i(MyStorage.LOG_TAG, cursor.getString(0));
            }
        }catch (Exception e){
            Log.e(MyStorage.LOG_TAG, "", e);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            long currTime = System.currentTimeMillis();
            if((currTime - prevTime) >= MyStorage.DOUBLE_BACK_EXIT_TIME){
                Toast.makeText(this, R.string.exit_app_msg, Toast.LENGTH_SHORT).show();
            }else{
                ActivityManager.closeApp();
            }
            prevTime = currTime;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}