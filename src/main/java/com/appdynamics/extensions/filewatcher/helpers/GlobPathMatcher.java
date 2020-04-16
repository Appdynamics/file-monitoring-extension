/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.helpers;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.extensions.filewatcher.config.PathToProcess;

import java.nio.file.FileSystems;

public class GlobPathMatcher extends AppPathMatcher{

    @Override
    public void setMatcher(PathToProcess fileToProcess) {
        this.file = fileToProcess;
        this.matcher= FileSystems.getDefault().getPathMatcher("glob:"+ this.file.getPath());
    }
}