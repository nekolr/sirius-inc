package com.nekolr.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 日志实体
 */
@Getter
@Setter
@ToString
public class LogEntry {
    /**
     * 版本号
     */
    private String revision;
    /**
     * 作者
     */
    private String author;
    /**
     * 提交时间
     */
    private String date;
    /**
     * 地址列表
     */
    private List<Path> paths;

    /**
     * 提交信息
     */
    private String msg;

    /**
     * 地址实体
     */
    @Getter
    @Setter
    @ToString
    public static class Path {

        /**
         * 类型
         */
        private String kind;

        /**
         * 动作
         */
        private String action;

        /**
         * 路径
         */
        private String path;
    }
}
