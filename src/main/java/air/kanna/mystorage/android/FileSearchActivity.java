package air.kanna.mystorage.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import air.kanna.mystorage.MyStorage;
import air.kanna.mystorage.android.adapter.FileListAdapter;
import air.kanna.mystorage.dao.DiskDescriptionDAO;
import air.kanna.mystorage.dao.FileItemDAO;
import air.kanna.mystorage.dao.OrderBy;
import air.kanna.mystorage.dao.Pager;
import air.kanna.mystorage.dao.condition.FileItemCondition;
import air.kanna.mystorage.dao.impl.android.sqlite.DiskDescriptionDAOAndroidSqliteImpl;
import air.kanna.mystorage.dao.impl.android.sqlite.FileItemDAOAndroidSqliteImpl;
import air.kanna.mystorage.model.DiskDescription;
import air.kanna.mystorage.model.FileItem;
import air.kanna.mystorage.model.FileType;
import air.kanna.mystorage.model.MyStorageConfig;
import air.kanna.mystorage.service.DataBaseService;
import air.kanna.mystorage.service.DiskDescriptionService;
import air.kanna.mystorage.service.FileItemService;
import air.kanna.mystorage.service.impl.AndroidSqliteDataBaseServiceImpl;
import air.kanna.mystorage.service.impl.DiskDescriptionServiceImpl;
import air.kanna.mystorage.service.impl.FileItemServiceImpl;
import air.kanna.mystorage.sync.model.ConnectParam;
import air.kanna.mystorage.sync.process.LocalFileReceiveSyncProcess;
import air.kanna.mystorage.util.StringUtil;
import kanna.air.mystorage.android.R;

public class FileSearchActivity extends BasicActivity {
    private static final int MSG_START_LOADING = 1;
    private static final int MSG_END_LOADING = 2;
    private static final int MSG_SHOW_ERROR_DIALOG = 3;
    private static final int MSG_SHOW_INFOR_DIALOG = 4;
    private static final int MSG_DO_SEARCH = 5;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private GridLayoutManager recyclerLayout;
    private MenuItem searchItem;
    private MenuItem syncItem;
    private FileListAdapter adapter;
    private Handler mHandler;

    private AlertDialog searchDialog = null;
    private View dialogRoot = null;
    private EditText fileName = null;
    private Spinner fileType = null;
    private Spinner diskSelected = null;

    private Dialog processDialog = null;
    private AndroidProcessListener processListener;
    private TextView processMsg = null;
    private ProgressBar processBar = null;

    private MyStorageConfig config;
    private DataBaseService<SQLiteDatabase> dataBaseService;
    private DiskDescriptionService diskService;
    private FileItemService fileItemService;
    private FileItemCondition condition;
    private Pager pager;
    private final OrderBy order = getDefaultOrder();
    private List<DiskDescription> diskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerLayout = new GridLayoutManager(current, 1);

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MSG_START_LOADING){
                    swipeRefreshLayout.setRefreshing(true);
                }else
                if(msg.what == MSG_END_LOADING){
                    swipeRefreshLayout.setRefreshing(false);
                }else
                if(msg.what == MSG_SHOW_ERROR_DIALOG){
                    Toast.makeText(current, msg.obj.toString() , Toast.LENGTH_LONG).show();
//                    showErrorMessage(msg.obj.toString(), null);
                }else
                if(msg.what == MSG_SHOW_INFOR_DIALOG){
                    showInformationMessage(msg.obj.toString(), null);
                }
                if(msg.what == MSG_DO_SEARCH){
                    doSearch();
                }
                super.handleMessage(msg);
            }
        };

        checkAndInitData();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(MyStorage.LOG_TAG, "下拉刷新");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        doSearch();
                    }
                });
            }
        });

        initRecyclerView();
        doSearch();
    }

    private void doSearch(){
        condition = getConditionFromConfig(config);
        pager = new Pager();
        pager.setSize(config.getPageSize());

        mHandler.sendEmptyMessage(MSG_START_LOADING);

        updateRecyclerView(false);
    }

    private void checkAndInitData(){
        try{
            config = ServiceFactory.getConfigService().getConfig();

            File basePath = new File(config.getSelectedPath());
            if(!basePath.exists()){
                if(!basePath.mkdirs()){
                    showErrorMessage(R.string.dbpath_create_error, null);
                    return;
                }
            }
            File dbFile = new File(basePath, config.getDbFileName());
            if(!dbFile.exists() || !dbFile.isFile()){
                showInformationMessage(R.string.dbfile_not_found, null);
                return;
            }
            SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            dataBaseService = new AndroidSqliteDataBaseServiceImpl(current);
            String check = dataBaseService.checkDataBase(sqLiteDatabase);
            if(check != null){
                showErrorMessage(check, null);
            }
            DiskDescriptionDAO diskDao = new DiskDescriptionDAOAndroidSqliteImpl(sqLiteDatabase);
            FileItemDAO fileDao = new FileItemDAOAndroidSqliteImpl(sqLiteDatabase);

            diskService = new DiskDescriptionServiceImpl();
            ((DiskDescriptionServiceImpl)diskService).setModelDao(diskDao);

            fileItemService = new FileItemServiceImpl();
            ((FileItemServiceImpl)fileItemService).setModelDao(fileDao);
            ((FileItemServiceImpl)fileItemService).setDiskService(diskService);

            diskList = diskService.listAll(null, null);
        }catch (Exception e){
            Log.e(MyStorage.LOG_TAG, getString(R.string.init_data_error), e);
            showErrorMessage(getString(R.string.init_data_error) + e.getMessage(), null);
            fileItemService = null;
        }
    }

    private void initRecyclerView(){
        adapter = new FileListAdapter(current);
        recyclerView.setLayoutManager(recyclerLayout);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(current, DividerItemDecoration.VERTICAL));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItemIndex = -1;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 在newState为滑到底部时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(swipeRefreshLayout.isRefreshing()){
                        return;
                    }
                    //最后一个条目的位置就比我们的getItemCount少1，自己可以算一下
                    if ((lastVisibleItemIndex + 1) == adapter.getItemCount()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 然后调用updateRecyclerview方法更新RecyclerView
                                if(pager.getPage() >= pager.getMaxPage()){
                                    return;
                                }
                                pager.setPage(pager.getPage() + 1);
                                updateRecyclerView(true);
                            }
                        }, 500);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 在滑动完成后，拿到最后一个可见的item的位置
                lastVisibleItemIndex = recyclerLayout.findLastVisibleItemPosition();
            }
        });
    }

    private void updateRecyclerView(boolean isAdd) {
        if(fileItemService == null){
            return;
        }
        if(pager.getTotal() <= 0){
            pager.setTotal(fileItemService.getByConditionCount(condition));
        }

        List<FileItem> itemList = fileItemService.getByCondition(condition, order, pager);
        if(isAdd) {
            adapter.updateAndAddList(itemList, pager);
        }else{
            adapter.resetDatas(itemList, pager);
        }

        mHandler.sendEmptyMessage(MSG_END_LOADING);
    }

    private FileItemCondition getConditionFromConfig(MyStorageConfig config){
        FileItemCondition condition = new FileItemCondition();
        if(config == null){
            return condition;
        }
        if(StringUtil.isNotSpace(config.getSearchFileName())){
            condition.setFileName(config.getSearchFileName());
        }
        if(StringUtil.isNotNull(config.getSearchFileType())) {
            if("D".equalsIgnoreCase(config.getSearchFileType())) {
                condition.setFileType('D');
            }else
            if("F".equalsIgnoreCase(config.getSearchFileType())) {
                condition.setFileType('F');
            }
        }
        DiskDescription disk = getDiskByPath(config.getSearchDiskPath());
        if(disk != null){
            condition.setDiskId(disk.getId());
        }
        return condition;
    }

    private DiskDescription getDiskByPath(String path){
        if(diskList == null || diskList.size() <= 0){
            return null;
        }
        if(StringUtil.isSpace(path)){
            return null;
        }
        for(DiskDescription disk : diskList){
            if(disk.getBasePath().equalsIgnoreCase(path)){
                return disk;
            }
        }
        return null;
    }

    private OrderBy getDefaultOrder() {
        OrderBy defOrder = new OrderBy();

        defOrder.addOrderAsc("disk_id");
        defOrder.addOrderAsc("file_name");

        return defOrder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_view_list, menu);

        searchItem = menu.findItem(R.id.actionbar_search_btn);
        syncItem = menu.findItem(R.id.actionbar_sync_btn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View searchItemView = findViewById(R.id.actionbar_search_btn);

                searchItemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showInformationMessage("searchItemView long click", null);
                        return true;
                    }
                });
            }
        }, 1000);

        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showSearch();
                return true;
            }
        });

        syncItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showScanSync();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private AlertDialog getSeatchDialog(){
        if(searchDialog != null){
            return searchDialog;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(current);
        dialogRoot = current.getLayoutInflater().inflate(R.layout.dialog_search_param, null);
        fileName = dialogRoot.findViewById(R.id.dialog_search_file_name_edit);
        fileType = dialogRoot.findViewById(R.id.dialog_search_type_spinner);
        diskSelected = dialogRoot.findViewById(R.id.dialog_search_disk_spinner);

        List<String> fileTypeData = Arrays.asList(
                getString(R.string.dialog_search_all),
                getString(R.string.dialog_search_type_file),
                getString(R.string.dialog_search_type_path)
        );

        List<String> diskData = new ArrayList<>();
        diskData.add(getString(R.string.dialog_search_all));
        for(DiskDescription disk : diskList){
            diskData.add(disk.getBasePath());
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(
                current, android.R.layout.simple_spinner_dropdown_item, fileTypeData);
        ArrayAdapter<String> diskAdapter = new ArrayAdapter<String>(
                current, android.R.layout.simple_spinner_dropdown_item, diskData);

        fileType.setAdapter(typeAdapter);
        diskSelected.setAdapter(diskAdapter);

        builder.setView(dialogRoot)
                .setCancelable(false)
                .setTitle(R.string.dialog_search_title)
                .setNeutralButton(R.string.clear_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        config.setSearchFileName("");
                        fileName.setText("");

                        config.setSearchFileType("");
                        fileType.setSelection(0);

                        config.setSearchDiskPath("");
                        diskSelected.setSelection(0);
                        ServiceFactory.getConfigService().saveConfig(config);
                        doSearch();
                    }
                })
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        config.setSearchFileName(fileName.getText().toString());
                        if(fileType.getSelectedItemPosition() == 1) {
                            config.setSearchFileType("" + FileType.TYPE_FILE.getType());
                        }else if(fileType.getSelectedItemPosition() == 2){
                            config.setSearchFileType("" + FileType.TYPE_DICECTORY.getType());
                        }else{
                            config.setSearchFileType("");
                        }
                        int diskIndex = diskSelected.getSelectedItemPosition() - 1;
                        if(diskIndex >= 0 && diskIndex < diskList.size()){
                            config.setSearchDiskPath(diskList.get(diskIndex).getBasePath());
                        }
                        ServiceFactory.getConfigService().saveConfig(config);
                        doSearch();
                    }
                })
                .setNegativeButton(R.string.cancel_button, null);

        return builder.create();
    }

    private void showSearch(){
        AlertDialog dialog = getSeatchDialog();
        int selected = -1;

        fileName.setText(config.getSearchFileName());
        if(StringUtil.isSpace(config.getSearchFileType())){
            fileType.setSelection(0);
        }else{
            if(config.getSearchFileType().equalsIgnoreCase("" + FileType.TYPE_FILE.getType())){
                fileType.setSelection(1);
            }else{
                fileType.setSelection(2);
            }
        }
        for(int i=0; i<diskList.size(); i++){
            if(diskList.get(i).getBasePath().equalsIgnoreCase(config.getSearchDiskPath())){
                selected = i;
                break;
            }
        }
        if(selected < 0){
            diskSelected.setSelection(0);
        }else{
            diskSelected.setSelection(selected + 1);
        }

        dialog.show();
    }

    private void showInputSync(){

    }

    private void showScanSync(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);

        integrator.initiateScan();
    }

    private void cleanUnsucessFiles(File path){
        File[] files = path.listFiles();
        for(int i=0; i<files.length; i++){
            if(!files[i].isFile()){
                continue;
            }
            if(!files[i].exists()){
                continue;
            }
            if(!files[i].getName().startsWith(config.getDbFileName())){
                continue;
            }
            if(!files[i].getName().endsWith(LocalFileReceiveSyncProcess.TRANS_TEMP_FILE_END)){
                continue;
            }
            files[i].delete();
        }
    }
    private void doSync(final ConnectParam param){
        if(param == null){
            return;
        }
        final File basePath = new File(config.getSelectedPath());
        if(!basePath.exists()){
            if(!basePath.mkdirs()){
                showErrorMessage(R.string.dbpath_create_error, null);
                return;
            }
        }
        try{
            cleanUnsucessFiles(basePath);
            getProgressDialog().show();
            new Thread(){
                @Override
                public void run() {
                    LocalFileReceiveSyncProcess process = null;
                    boolean isSuccess = false;
                    try {
                        process = new LocalFileReceiveSyncProcess(param, basePath);
                        Socket socket = new Socket(param.getIp(), param.getPort());

                        process.setListener(processListener);
                        process.start(socket);
//                        Toast.makeText(current, R.string.sync_success_msg , Toast.LENGTH_SHORT).show();
                        isSuccess = true;
                    }catch(Exception e){
                        processDialog.dismiss();
                        Log.e(MyStorage.LOG_TAG, "error", e);
                        Message msg = new Message();
                        msg.what = MSG_SHOW_ERROR_DIALOG;
                        msg.obj = current.getString(R.string.sync_error_msg) + e.getMessage();
                        mHandler.sendMessage(msg);
                    }finally {
                        processDialog.dismiss();
                        if(isSuccess) {
                            checkAndInitData();
                            mHandler.sendEmptyMessage(MSG_DO_SEARCH);
                        }
                        if(process != null && !process.isFinish()) {
                            try {
                                process.finish();
                            } catch (Exception e) {
                                Log.e(MyStorage.LOG_TAG, "finish error", e);
                                if(!e.getMessage().equalsIgnoreCase("Socket closed")){
                                    Message msg = new Message();
                                    msg.what = MSG_SHOW_ERROR_DIALOG;
                                    msg.obj = current.getString(R.string.sync_error_msg) + e.getMessage();
                                    mHandler.sendMessage(msg);
                                }
                            }
                        }
                    }
                }
            }.start();
        }catch (Exception e){
            Log.e(MyStorage.LOG_TAG, "error", e);
            showErrorMessage(current.getString(R.string.sync_error_msg) + e.getMessage(), null);
        }
    }

    private Dialog getProgressDialog(){
        if(processDialog != null){
            return processDialog;
        }
        processDialog = new Dialog(current);
        processDialog.setContentView(R.layout.dialog_sync_process);

        processMsg = processDialog.findViewById(R.id.dialog_sync_proc_msg);
        processBar = processDialog.findViewById(R.id.dialog_sync_proc_bar);
        processListener = new AndroidProcessListener(processMsg, processBar);
        return processDialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        ConnectParam syncParam = null;
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String resultStr = scanResult.getContents();
            try {
                syncParam = JSON.parseObject(resultStr, ConnectParam.class);
            } catch (Exception e) {
                Log.e(MyStorage.LOG_TAG, "Parse ConnectParam error.", e);
            }
        }
        if(syncParam == null){
            new AlertDialog.Builder(current)
                    .setTitle(R.string.dialog_title_error)
                    .setMessage(R.string.sync_param_scan_error_msg)
                    .setPositiveButton(R.string.rescan_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showScanSync();
                        }
                    })
                    .setNegativeButton(R.string.cancel_button, null)
                    .setNeutralButton(R.string.input_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }else{
            doSync(syncParam);
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
