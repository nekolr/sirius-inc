package com.nekolr.util;

/**
 * 操作系统工具类
 *
 * @author nekolr
 */
public class OSUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private OSUtils() {
    }

    public static boolean isLinux() {
        return OS.indexOf("linux") != -1;
    }

    public static boolean isMacOS() {
        return OS.indexOf("mac") != -1 && OS.indexOf("os") != -1 && OS.indexOf("x") == -1;
    }

    public static boolean isMacOSX() {
        return OS.indexOf("mac") != -1 && OS.indexOf("os") != -1 && OS.indexOf("x") != -1;
    }

    public static boolean isWindows() {
        return OS.indexOf("windows") != -1;
    }

    public static String getOS() {
        return OS;
    }
}
