package com.appdynamics.extensions.filewatcher.pathmatcher.helpers;

import java.io.File;
import java.nio.file.Path;

public class DisplayNameHelper {

	public static String getFormattedDisplayName(String fileDisplayName,Path path,String baseDir){
		StringBuilder builder = new StringBuilder();
		builder.append(fileDisplayName);
		builder.append(path.toString().replaceAll(baseDir.substring(0, baseDir.length()-1), "")
					.replaceAll(File.separator, "|"));

		return builder.toString();
	}

}
