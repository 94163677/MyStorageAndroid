package air.kanna.mystorage.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import java.io.File;
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
    private FileListAdapter adapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());

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
                        if(condition == null){
                            condition = getConditionFromConfig(config);
                        }
                        pager = new Pager();
                        updateRecyclerView();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        initRecyclerView();

        condition = getConditionFromConfig(config);
        pager = new Pager();
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
                                updateRecyclerView();
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

    private void updateRecyclerView() {
        if(fileItemService == null){
            return;
        }
        if(pager.getTotal() <= 0){
            pager.setTotal(fileItemService.getByConditionCount(condition));
        }

        List<FileItem> itemList = fileItemService.getByCondition(condition, order, pager);
        adapter.updateAndAddList(itemList, pager.getPage() >= pager.getMaxPage());
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
        return super.onCreateOptionsMenu(menu);
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
