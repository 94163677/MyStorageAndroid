package air.kanna.mystorage.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
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
import air.kanna.mystorage.util.StringUtil;
import kanna.air.mystorage.android.R;

public class FileSearchActivity extends BasicActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private GridLayoutManager recyclerLayout;
    private MenuItem searchItem;
    private MenuItem syncItem;
    private FileListAdapter adapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private AlertDialog searchDialog = null;
    private View dialogRoot = null;
    private EditText fileName = null;
    private Spinner fileType = null;
    private Spinner diskSelected = null;

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

        checkAndInitData();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(MyStorage.LOG_TAG, "下拉刷新");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        doSearch(false);
                    }
                });
            }
        });

        initRecyclerView();
        doSearch(true);
    }

    private void doSearch(boolean isInit){
        if(isInit){
            condition = new FileItemCondition();
        }else {
            condition = getConditionFromConfig(config);
        }
        pager = new Pager();
        pager.setSize(config.getPageSize());
        swipeRefreshLayout.setRefreshing(true);
        updateRecyclerView(false);
    }

    private void checkAndInitData(){
        try{
            config = ServiceFactory.getConfigService().getConfig();

            File basePath = new File(config.getSelectedPath());
            if(!basePath.exists()){
                basePath.mkdirs();
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
        swipeRefreshLayout.setRefreshing(false);
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

        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showSearch();
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
                        doSearch(true);
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
                        doSearch(false);
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
