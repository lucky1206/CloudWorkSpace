package com.acs.model.file;

import java.text.SimpleDateFormat;

public class FileInfo {
    private String dbName;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private String fileType;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    private long fileSize;

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    private String uploadDate;

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.uploadDate = formatter.format(Long.parseLong(uploadDate));
    }

    /**
     * 文件处理状态，-1：待处理入库或入库处理异常，需要重新处理入库；1：已处理入库,2:忽略状态，查询所有记录
     */
    private int fileState;

    public int getFileState() {
        return fileState;
    }

    public void setFileState(int fileState) {
        this.fileState = fileState;
    }

    /**
     * 文件元数据
     */
    private String fileDesc;

    public String getFileDesc() {
        return fileDesc;
    }

    public void setFileDesc(String fileDesc) {
        this.fileDesc = fileDesc;
    }

    /**
     * 文件建表sql文本
     */
    private String ctsql;

    public String getCtsql() {
        return ctsql;
    }

    public void setCtsql(String ctsql) {
        this.ctsql = ctsql;
    }
}
