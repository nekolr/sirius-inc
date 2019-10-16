package com.nekolr;

import java.io.File;

/**
 * 常量类
 *
 * @author nekolr
 */
public class Constants {

    /**
     * Windows 下 svn log 带用户名密码命令模板
     */
    public static final String SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_WINDOWS = "cmd /c svn log --xml --no-auth-cache --username {0} --password {1} -v {2} {3} > {4}";

    /**
     * Linux 下 svn log 带用户名密码命令模板
     */
    public static final String SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_LINUX = "svn log --xml --no-auth-cache --username={0} --password={1} -v {2} {3}";

    /**
     * Mac OS/Mac OS X 下 svn log 带用户名密码命令模板
     */
    public static final String SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_SHELL = "svn log --xml --no-auth-cache --username={0} --password={1} -v {2} {3} > {4}";

    /**
     * Windows 下 svn log 命令模板
     */
    public static final String SVN_LOG_COMMAND_TEMPLATE_FOR_WINDOWS = "cmd /c svn log --xml -v {0} {1} > {2}";

    /**
     * Linux 下 svn log 命令模板
     */
    public static final String SVN_LOG_COMMAND_TEMPLATE_FOR_LINUX = "svn log --xml -v {0} {1}";

    /**
     * Mac OS/Mac OS X 下 svn log 命令模板
     */
    public static final String SVN_LOG_COMMAND_TEMPLATE_FOR_SHELL = "svn log --xml -v {0} {1} > {2}";

    /**
     * 如果配置默认的输出路径为 USER_PRO_FILE，则表示使用用户目录
     */
    public static final String DEFAULT_OUT_PATH = "USER_PRO_FILE";

    /**
     * java 源代码一般所在目录前缀
     */
    public static final String JAVA_SRC_PREFIX = "src/main/java";

    /**
     * 资源文件一般所在目录前缀
     */
    public static final String RESOURCE_PREFIX = "src/main/resources";

    /**
     * java 测试源代码一般所在目录前缀
     */
    public static final String JAVA_TEST_SRC_PREFIX = "src/test/java";

    /**
     * 测试资源文件一般所在目录前缀
     */
    public static final String RESOURCE_TEST_PREFIX = "src/test/resources";

    /**
     * Maven POM 文件
     */
    public static final String POM_FILE = "pom.xml";

    /**
     * 其他文件一般所在目录前缀
     */
    public static final String OTHER_PREFIX = "src/main/webapp";

    /**
     * 用户上次配置存放的文件
     */
    public static final String USER_LAST_SETTING_FILE = System.getProperty("user.home") + File.separator + "sirius_inc_last_setting.yml";

    /**
     * 标题
     */
    public static final String TITLE = "Sirius 增量包生成工具 0.8.0";
}
