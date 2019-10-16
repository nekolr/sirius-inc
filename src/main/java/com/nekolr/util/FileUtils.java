package com.nekolr.util;

import cn.hutool.core.io.FileUtil;

import java.io.File;

/**
 * @author nekolr
 */
public class FileUtils {

    /**
     * 删除文件
     *
     * @param filepath
     */
    public static void deleteFile(String filepath) {
        FileUtil.del(filepath);
    }

    /**
     * 是否是目录
     *
     * @param filePath
     * @return
     */
    public static boolean isDirectory(String filePath) {
        return filePath.lastIndexOf(".") == -1;
    }

    /**
     * 在 windows 系统中打开对应的目录
     *
     * @param targetDir
     */
    public static void openDirectoryUnderWindows(String targetDir) {
        // 只在 Windows 下才主动打开文件夹
        if (OSUtils.isWindows()) {
            String command = "cmd /c start " + targetDir;
            CommandUtils.exec(command);
        }
    }

    /**
     * 处理目录最后的分隔符
     *
     * @param dir
     * @return
     */
    public static String processDirectoryPath(String dir) {
        // 处理最后的分隔符
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.lastIndexOf("/"));
        }
        if (dir.endsWith("\\")) {
            dir = dir.substring(0, dir.lastIndexOf("/"));
        }
        if (dir.endsWith(File.separator)) {
            dir = dir.substring(0, dir.lastIndexOf(File.separator));
        }
        return dir;
    }
}
