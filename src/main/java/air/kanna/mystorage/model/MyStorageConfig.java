package air.kanna.mystorage.model;

public class MyStorageConfig {
    //默认每页显示的数据量
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * 系统相关的设置
     */
    //用户选择存放的基础位置
    private String selectedPath = "";
    //数据库文件名称
    private String dbFileName = "MyStorage.db";
    
    
    /**
     * 用户输入相关的保存性设置
     */
    //搜索文件名称
    private String searchFileName = "";
    //搜索文件类型
    private String searchFileType = "";
    //搜索指定的磁盘
    private String searchDiskPath = "";
    
    
    /**
     * 用户相关的设置
     */
    //每页显示的数量
    private int pageSize = DEFAULT_PAGE_SIZE;


    public String getSelectedPath() {
        return selectedPath;
    }
    public void setSelectedPath(String selectedPath) {
        this.selectedPath = selectedPath;
    }
    public String getDbFileName() {
        return dbFileName;
    }
    public void setDbFileName(String dbFileName) {
        this.dbFileName = dbFileName;
    }
    public String getSearchFileName() {
        return searchFileName;
    }
    public void setSearchFileName(String searchFileName) {
        this.searchFileName = searchFileName;
    }
    public String getSearchFileType() {
        return searchFileType;
    }
    public void setSearchFileType(String searchFileType) {
        this.searchFileType = searchFileType;
    }
    public String getSearchDiskPath() {
        return searchDiskPath;
    }
    public void setSearchDiskPath(String searchDiskPath) {
        this.searchDiskPath = searchDiskPath;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
