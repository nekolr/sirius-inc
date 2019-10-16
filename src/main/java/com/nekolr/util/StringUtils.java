package com.nekolr.util;

import java.util.Arrays;
import java.util.List;

/**
 * 字符串工具类
 *
 * @author nekolr
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * 拆解字符串，没有考虑空的问题
     *
     * @param str
     * @param regex
     * @return
     */
    public static List<String> split(String str, String regex) {
        String[] splits = str.split(regex);
        return Arrays.asList(splits);
    }

    /**
     * 判断字符串是否为空
     *
     * @param content
     * @return
     */
    public static boolean isEmpty(String content) {
        return content == null || "".equals(content);
    }

}
