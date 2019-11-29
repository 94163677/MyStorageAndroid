package air.kanna.mystorage.android.vo;

import air.kanna.mystorage.model.DiskDescription;
import air.kanna.mystorage.model.FileItem;

public class FileItemVo {
    private static final int DIG_SIZE = 100;//保留2位小数
    private static final long KB_SIZE = 1024;
    private static final long MB_SIZE = KB_SIZE * 1024;
    private static final long GB_SIZE = MB_SIZE * 1024;
    private static final long TB_SIZE = GB_SIZE * 1024;

    private String diskPath = "";
    private String diskDesc = "";

    private String fileName = "";
    private String filePath = "";
    private String fileType = "";

    private String fileSizeByte = "";
    private String fileSize = "";
    private String fileMD5 = "";
    private String fileSHA256 = "";

    private String createDate = "";
    private String lastModDate = "";

    public FileItemVo(FileItem item){
        if(item == null){
            throw new NullPointerException("FileItem is null");
        }
        DiskDescription disk = item.getDiskDescription();
        if(disk != null){
            diskPath = disk.getBasePath();
            diskDesc = disk.getDescription();
        }
        fileName = item.getFileName();
        filePath = item.getFilePath();
        fileType = item.getFileTypeObj().getDescription();//TODO 多语言支持

        fileSizeByte = item.getFileSize() + "B";
        fileSize = getStorageSize(item.getFileSize());
        fileMD5 = item.getFileHash01() == null ? "" : item.getFileHash01();
        fileSHA256 = item.getFileHash02() == null ? "" : item.getFileHash02();

        createDate = item.getCreateDateStr();
        lastModDate = item.getLastModDateStr();
    }

    private String getStorageSize(long size){
        StringBuilder sb = new StringBuilder();
        String end;
        int num = 0, dig = 0;
        long fixSize = 1;

        if(size > TB_SIZE){
            fixSize = TB_SIZE;
            end = "TB";
        }else if(size > GB_SIZE) {
            fixSize = GB_SIZE;
            end = "GB";
        }else if(size > MB_SIZE){
            fixSize = MB_SIZE;
            end = "MB";
        }else if(size > KB_SIZE){
            fixSize = KB_SIZE;
            end = "KB";
        }else{
            end = "B";
        }

        if(fixSize != 1) {
            num = (int) (size / fixSize);
            dig = (int) ((DIG_SIZE * (size % fixSize)) / fixSize);
        }else{
            num = (int)size;
            dig = 0;
        }

        sb.append(num);
        if(dig != 0){
            sb.append('.').append(dig);
        }
        sb.append(end);
        return sb.toString();
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }

    public String getDiskDesc() {
        return diskDesc;
    }

    public void setDiskDesc(String diskDesc) {
        this.diskDesc = diskDesc;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSizeByte() {
        return fileSizeByte;
    }

    public void setFileSizeByte(String fileSizeByte) {
        this.fileSizeByte = fileSizeByte;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public String getFileSHA256() {
        return fileSHA256;
    }

    public void setFileSHA256(String fileSHA256) {
        this.fileSHA256 = fileSHA256;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(String lastModDate) {
        this.lastModDate = lastModDate;
    }
}
