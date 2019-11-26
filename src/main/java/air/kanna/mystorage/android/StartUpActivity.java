package air.kanna.mystorage.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import air.kanna.mystorage.MyStorage;
import air.kanna.mystorage.model.MyStorageConfig;
import air.kanna.mystorage.service.MyStorageConfigService;
import air.kanna.mystorage.service.PasswordService;
import air.kanna.mystorage.service.impl.MyStorageConfigServiceSharedPreferencesImpl;
import air.kanna.mystorage.service.impl.SharedPreferencesPasswordServiceImpl;
import air.kanna.mystorage.util.StringUtil;
import kanna.air.mystorage.android.R;

public class StartUpActivity extends BasicActivity {
    private EditText password;
    private Button okBtn;
    private PasswordService passService;
    private MyStorageConfigService configService;
    private MyStorageConfig config;

    private List<File> basePathList;
    private int selectedFileIndex = -1;

    private long prevTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        if(!checkStoragePermissionAndApply()){
            return;
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
                    new AlertDialog.Builder(current)
                            .setTitle(R.string.dialog_title_error)
                            .setMessage(R.string.apply_storage_msg)
                            .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkStoragePermissionAndApply();
                                }
                            })
                            .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityManager.closeApp();
                                }
                            })
                            .show();
                }else{
                    init();
                }
            };break;
            default: Log.w(MyStorage.LOG_TAG, "Cannot support requestCode: " + requestCode);
        }
    }

    private void init(){
        password = findViewById(R.id.startup_password_text);
        okBtn = findViewById(R.id.startup_ok_btn);
        passService = new SharedPreferencesPasswordServiceImpl(this);
        configService = new MyStorageConfigServiceSharedPreferencesImpl(this);

        ServiceFactory.registeredPasswordService(passService);
        ServiceFactory.registeredConfigService(configService);

        if(!passService.hasPassword()){
            password.setHint(R.string.set_password);
        }

        config = configService.getConfig();
        basePathList = new ArrayList<>();
//        config.setSelectedPath("");

        if(StringUtil.isSpace(config.getSelectedPath())){
            List<String> selectPaths = new ArrayList<>();
            File[] list = getExternalFilesDirs(Environment.MEDIA_MOUNTED);
            for(int i=0; i<list.length; i++){
                if(list[i] == null){
                    continue;
                }
                Log.i(MyStorage.LOG_TAG, list[i].getAbsolutePath());
                String name = list[i].getAbsolutePath().toLowerCase();
                if(name.indexOf("emulated") > 0){
                    selectPaths.add("内部存储");
                }else{
                    selectPaths.add("外部存储");
                }
                basePathList.add(list[i]);
            }
            if(selectPaths.size() <= 0){
                new AlertDialog.Builder(current)
                        .setTitle(R.string.dialog_title_error)
                        .setMessage(R.string.base_path_not_found)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityManager.closeApp();
                            }
                        })
                        .show();
            }
            final String[] showSelect = new String[selectPaths.size()];
            for(int i=0; i<selectPaths.size(); i++){
                showSelect[i] = selectPaths.get(i);
            }
            selectedFileIndex = 0;
            new AlertDialog.Builder(current)
                    .setTitle(R.string.select_base_path)
                    .setSingleChoiceItems(showSelect, selectedFileIndex, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedFileIndex = which;
                        }
                    })
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(selectedFileIndex < 0 || selectedFileIndex >= basePathList.size()){
                                selectedFileIndex = 0;
                                Toast.makeText(current, R.string.select_default_path, Toast.LENGTH_SHORT).show();
                            }
                            config.setSelectedPath(basePathList.get(selectedFileIndex).getAbsolutePath());
                            configService.saveConfig(config);
                            Log.i(MyStorage.LOG_TAG, basePathList.get(selectedFileIndex).getAbsolutePath());
                        }
                    })
                    .show();
        }else{
            Log.i(MyStorage.LOG_TAG, config.getSelectedPath());
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

    private boolean checkStoragePermissionAndApply(){
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        for(int i=0; i<permissions.length; i++){
            if(ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                requestPermission(permissions, REQ_PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

    private void nextActivity(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        intent.setClass(this, MainActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
