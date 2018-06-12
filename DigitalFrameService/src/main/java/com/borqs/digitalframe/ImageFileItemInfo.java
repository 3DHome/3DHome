package com.borqs.digitalframe;

public class ImageFileItemInfo {

    private String mName;
    private long mId;
    private String mFolderName;
    private String mFolderId;
    private String mPath;
    private String mFolderPath;

    public ImageFileItemInfo() {

    }

    void setDisplayName(String name) {
        mName = name;
    }

    void setId(long id) {
        mId = id;
    }

    void setFolderName(String foldername) {
        mFolderName = foldername;
    }

    void setFolderId(String folderid) {
        mFolderId = folderid;
    }

    void setPath(String path) {
        mPath = path;
    }

    String getDisplayName() {
        return mName;
    }

    long getId() {
        return mId;
    }

    String getFolderName() {
        return mFolderName;
    }

    String getFolderId() {
        return mFolderId;
    }

    String getPath() {
        return mPath;
    }

    void setFolderPath(String folderpath) {
        mFolderPath = folderpath;
    }

    String getFolderPath() {
        return mFolderPath;
    }

}
