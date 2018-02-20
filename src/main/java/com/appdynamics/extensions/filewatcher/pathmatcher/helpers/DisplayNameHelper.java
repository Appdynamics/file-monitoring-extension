/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.filewatcher.pathmatcher.helpers;

import java.io.File;
import java.nio.file.Path;

import org.apache.log4j.Logger;

public class DisplayNameHelper {
	public static String getFormattedDisplayName(String fileDisplayName,Path path,String baseDir){
		StringBuilder builder = new StringBuilder();
		builder.append(fileDisplayName);
		String suffix = path.toString().replace(baseDir.substring(0, baseDir.length()-1), "")
				.replace(File.separator, "|");
		if(!suffix.startsWith("|")){
			builder.append('|');
			builder.append(suffix);
		}
		else{
			builder.append(suffix);
		}
		return builder.toString();
	}

}
