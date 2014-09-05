package com.appdynamics.extensions.fileWatcher;

/**
 * Created by abhi.pandey on 9/4/14.
 */
public class FileMetric {

    private boolean isChanged;
    private String fileSize;
    private String timeStamp;

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getTimeStamp() { return timeStamp; }

    public void setTimeStamp(String timeStamp) {this.timeStamp = timeStamp;}


}
