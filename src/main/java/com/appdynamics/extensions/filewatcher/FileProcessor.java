package com.appdynamics.extensions.fileWatcher;

import com.appdynamics.extensions.fileWatcher.config.FileToProcess;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by abhi.pandey on 9/17/14.
 */
public class FileProcessor {

    protected final Logger logger = Logger.getLogger(FileProcessor.class.getName());
    private List<String> pathOfFiles = Lists.newArrayList();
    private Map<String, String> filesToProcessMap = Maps.newHashMap();

    public void setMETRIC_SEPARATOR(String METRIC_SEPARATOR) {
        this.METRIC_SEPARATOR = METRIC_SEPARATOR;
    }

    private String METRIC_SEPARATOR;

    public void createListOfPaths(List<FileToProcess> files) {
        for (FileToProcess fileToProcess : files) {
            File file = new File(fileToProcess.getPath());
            pathOfFiles.add(file.getAbsolutePath());
        }
    }

    public FileMetric getFileMetric(String filePath) {
        FileMetric fileMetric;

        File file = new File(filePath);
        if (file.exists()) {
            fileMetric = new FileMetric();
            fileMetric.setTimeStamp(String.valueOf(file.lastModified()));
            if (file.isFile()) {
                fileMetric.setFileSize(String.valueOf(file.length()));
            } else {
                fileMetric.setFileSize(String.valueOf(folderSize(file)));
            }
            if(file.isDirectory()){

                fileMetric.setNumberOfFiles(file.listFiles().length);
            }else{
                fileMetric.setNumberOfFiles(-1);
            }

        } else {
            logger.error("no file exist at path:  " + filePath);
            return null;
        }
        return fileMetric;
    }

    private long folderSize(File folder) {
        long length = 0;
        for (File file : folder.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public Map<String, String> processDisplayName(List<FileToProcess> files) {


        for (FileToProcess fileToProcess : files) {
            File file = new File(fileToProcess.getPath());
            String displayName = fileToProcess.getDisplayName();
            if (!Strings.isNullOrEmpty(displayName)) {
                if (file.isDirectory()) {
                    List<FileToProcess> directoryFiles = new ArrayList<FileToProcess>();
                    for (File f : file.listFiles()) {

                        if (!pathOfFiles.contains(f.getAbsolutePath())) {
                            FileToProcess fp = new FileToProcess();
                            fp.setPath(f.getAbsolutePath());
                            fp.setDisplayName(displayName.concat(METRIC_SEPARATOR).concat(f.getName()));
                            directoryFiles.add(fp);
                        }
                    }
                    processDisplayName(directoryFiles);
                }
            }

            filesToProcessMap.put(fileToProcess.getPath(), displayName);
        }
        return filesToProcessMap;
    }


}
