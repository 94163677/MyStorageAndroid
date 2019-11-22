package air.kanna.mystorage.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import air.kanna.mystorage.MyStorage;
import air.kanna.mystorage.service.PasswordService;
import air.kanna.mystorage.service.impl.SharedPreferencesPasswordServiceImpl;
import air.kanna.mystorage.util.StringUtil;
import kanna.air.mystorage.android.R;

public class StartUpActivity extends BasicActivity {
    private EditText password;
    private Button okBtn;
    private Activity current;

    private PasswordService passService;

    private long prevTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        for(int i=0; i<permissions.length; i++){
            if(ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                requestPermission(permissions, REQ_PERMISSION_STORAGE);
                return;
            }
        }

        init();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQ_PERMISSION_STORAGE: {
                boolean isDenied = false;
                for(int i=0; i<grantResults.length; i++){
                    if(PackageManager.PERMISSION_DENIED == grantResults[i]){
                        isDenied = true;
                        break;
                    }
                }
                if(isDenied){
                    ;//TODO 弹出框，之后退出app
                }else{
                    init();
                }
            };break;
            default: Log.w(MyStorage.LOG_TAG, "Cannot support requestCode: " + requestCode);
        }
    }

    private void init(){
        current = this;
        password = (EditText)findViewById(R.id.startup_password_text);
        okBtn = (Button)findViewById(R.id.startup_ok_btn);

        passService = new SharedPreferencesPasswordServiceImpl(this);

        if(!passService.hasPassword()){
            password.setHint(R.string.set_password);
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passStr = password.getText().toString();
                boolean hasPassword = passService.hasPassword();

                if(StringUtil.isSpace(passStr)){
                    if(hasPassword) {
                        Toast.makeText(current, R.string.input_password, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(current, R.string.set_password, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if(hasPassword){
                    if(!passService.checkPassword(passStr)){
                        Toast.makeText(current, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                        password.setText("");
                        return;
                    }
                }else{
                    if (!passService.createPassword(passStr)) {
                        Toast.makeText(current, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                nextActivity();
            }
        });
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

    private void nextActivity(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        intent.setClass(this, MainActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
