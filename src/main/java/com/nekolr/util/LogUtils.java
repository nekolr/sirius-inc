package com.nekolr.util;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.nekolr.Constants.HOME_PATH;

/**
 * 日志工具
 */
public class LogUtils {

    public static final String LOG_FILE = HOME_PATH + File.separator + "sirius_inc.log";

    private LogUtils() {

    }

    public static void write(String message) {
        List<String> lines = new ArrayList<>(1);
        // 时间戳
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        lines.add(now + " " + message);
        FileUtil.appendUtf8Lines(lines, LOG_FILE);
    }

    public static String getLogFile() {
        return LOG_FILE;
    }
}
