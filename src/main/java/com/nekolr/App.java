package com.nekolr;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.XmlUtil;
import com.nekolr.model.LogEntry;
import com.nekolr.model.Setting;
import com.nekolr.support.NoMatchFileFoundException;
import com.nekolr.util.ClassUtils;
import com.nekolr.util.CommandUtils;
import com.nekolr.util.OSUtils;
import com.nekolr.util.YmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主程序
 *
 * @author nekolr
 */
public class App {

    /**
     * Windows 下 svn log 带用户名密码命令模板
     */
    private static final String SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_WINDOWS = "cmd /c svn log --xml --no-auth-cache --username {0} --password {1} -v {2} {3} > {4}";

    /**
     * Linux 下 svn log 带用户名密码命令模板
     */
    private static final String SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_LINUX = "svn log --xml --no-auth-cache --username={0} --password={1} -v {2} {3}";

    /**
     * Mac OS/Mac OS X 下 svn log 带用户名密码命令模板
     */
    private static final String SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_SHELL = "svn log --xml --no-auth-cache --username={0} --password={1} -v {2} {3} > {4}";

    /**
     * Windows 下 svn log 命令模板
     */
    private static final String SVN_LOG_COMMAND_TEMPLATE_FOR_WINDOWS = "cmd /c svn log --xml -v {0} {1} > {2}";

    /**
     * Linux 下 svn log 命令模板
     */
    private static final String SVN_LOG_COMMAND_TEMPLATE_FOR_LINUX = "svn log --xml -v {0} {1}";

    /**
     * Mac OS/Mac OS X 下 svn log 命令模板
     */
    private static final String SVN_LOG_COMMAND_TEMPLATE_FOR_SHELL = "svn log --xml -v {0} {1} > {2}";

    /**
     * 如果配置默认的输出路径为 USER_PRO_FILE，则表示使用用户目录
     */
    private static final String DEFAULT_OUT_PATH = "USER_PRO_FILE";

    /**
     * java 源代码一般所在目录前缀
     */
    private static final String JAVA_SRC_PREFIX = "src/main/java";

    /**
     * 资源文件一般所在目录前缀
     */
    private static final String RESOURCE_PREFIX = "src/main/resources";

    /**
     * 其他文件一般所在目录前缀
     */
    private static final String OTHER_PREFIX = "src/main/webapp";

    /**
     * 逻辑入口
     */
    public static void run(Setting userSetting) throws Exception {
        // 获取全局配置
        Setting setting = getDefaultSetting(userSetting);
        // 生成 svn changelog 文件
        buildSvnChangelogFile(setting);
        // 解析 svn changelog 文件
        List<LogEntry> logEntryList = parseXml(setting.getSvnChangelogOutPath());
        // 解析完毕后删除 changelog 文件
        deleteFile(setting.getSvnChangelogOutPath());
        // 收集所有的文件路径
        List<String> filePathList = collectTargetFileList(logEntryList, setting.getCompiledProjectDir());
        // 生成更新包
        buildTargetUpdatePackage(setting.getTargetUpdatePackageDir(), filePathList, setting.getCompiledProjectDir());
        // 打开更新包目录
        openUpdatePackageDir(setting.getTargetUpdatePackageDir());
    }

    /**
     * 打开增量包目录
     *
     * @param targetUpdatePackageDir
     */
    private static void openUpdatePackageDir(String targetUpdatePackageDir) {
        // 只在 Windows 下才主动打开文件夹
        if (OSUtils.isWindows()) {
            String command = "cmd /c start " + targetUpdatePackageDir;
            CommandUtils.exec(command);
        }
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 生成更新包
     *
     * @param targetUpdatePackageDir
     * @param filePathList
     * @param compiledProjectDir
     */
    private static void buildTargetUpdatePackage(String targetUpdatePackageDir, List<String> filePathList, String compiledProjectDir) {
        for (String filePath : filePathList) {
            FileInputStream fileInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                File inputFile = new File(compiledProjectDir + File.separator + filePath);
                File outputFile = new File(targetUpdatePackageDir + File.separator + filePath);
                // 如果是目录，就只创建目录
                if (isDirectory(targetUpdatePackageDir + File.separator + filePath)) {
                    outputFile.mkdirs();
                    continue;
                }
                // 如果上级目录不存在就创建
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                fileInputStream = new FileInputStream(inputFile);
                fileOutputStream = new FileOutputStream(outputFile);
                IoUtil.copy(fileInputStream, fileOutputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 是否是目录
     *
     * @param filePath
     * @return
     */
    private static boolean isDirectory(String filePath) {
        return filePath.lastIndexOf(".") == -1;
    }

    /**
     * 收集所有的文件路径
     *
     * @param logEntries
     * @return
     */
    private static List<String> collectTargetFileList(List<LogEntry> logEntries, String compiledProjectDir) {
        List<String> filePathList = new ArrayList<>();
        if (logEntries != null) {
            for (LogEntry logEntry : logEntries) {
                List<LogEntry.Path> pathEntryList = logEntry.getPaths();
                for (LogEntry.Path pathEntry : pathEntryList) {
                    String targetFilePath;
                    String path = pathEntry.getPath();
                    // 如果是删除文件，则不做处理
                    if (!"D".equalsIgnoreCase(pathEntry.getAction())) {
                        if (path.indexOf(JAVA_SRC_PREFIX) != -1) {
                            targetFilePath = findJavaCompiledFile(path);
                            filePathList.addAll(findInnerClassFiles(compiledProjectDir, targetFilePath));
                        } else if (path.indexOf(RESOURCE_PREFIX) != -1) {
                            targetFilePath = findResourceFile(path);
                        } else if (path.indexOf(OTHER_PREFIX) != -1) {
                            targetFilePath = findOtherFile(path);
                        } else {
                            throw new NoMatchFileFoundException("svn 文件路径：[" + path + "] 没有找到对应的编译后文件");
                        }
                        filePathList.add(targetFilePath);
                    }
                }
            }
        }
        return filePathList;
    }

    /**
     * 处理目录最后的分隔符
     *
     * @param dir
     * @return
     */
    private static String postProcessDir(String dir) {
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

    /**
     * 寻找其他文件
     *
     * @param svnFilePath
     * @return
     */
    private static String findOtherFile(String svnFilePath) {
        // 加 16 是移动到匹配的文本末尾
        int otherIndex = svnFilePath.indexOf(OTHER_PREFIX) + 16;
        return svnFilePath.substring(otherIndex);
    }

    /**
     * 寻找 java 源代码对应的编译后的文件
     *
     * @param svnFilePath
     * @return
     */
    private static String findJavaCompiledFile(String svnFilePath) {
        // 加 14 是移动到匹配的文本末尾
        int srcIndex = svnFilePath.indexOf(JAVA_SRC_PREFIX) + 14;
        return "WEB-INF" + File.separator + "classes" +
                File.separator + svnFilePath.substring(srcIndex).replace("java", "class");
    }

    /**
     * 寻找资源文件
     *
     * @param svnFilePath
     * @return
     */
    private static String findResourceFile(String svnFilePath) {
        // 加 19 是移动到匹配的文本末尾
        int resourceIndex = svnFilePath.indexOf(RESOURCE_PREFIX) + 19;
        return "WEB-INF" + File.separator + "classes" + File.separator + svnFilePath.substring(resourceIndex);
    }


    /**
     * 寻找所有的内部类
     *
     * @param compiledProjectDir
     * @param classFilePath      class 文件部分路径
     * @return
     */
    private static List<String> findInnerClassFiles(String compiledProjectDir, String classFilePath) {
        File file = new File(compiledProjectDir + File.separator + classFilePath);
        List<String> fileList = new ArrayList<>();
        if (!file.isDirectory()) {
            String fileNameNoSuffix = file.getName().substring(0, file.getName().lastIndexOf("."));
            String prefix = classFilePath.substring(0, classFilePath.lastIndexOf(fileNameNoSuffix));
            File[] folderFiles = file.getParentFile().listFiles();
            for (File ele : folderFiles) {
                if (ele.getName().contains(fileNameNoSuffix + "$")) {
                    fileList.add(prefix + ele.getName());
                }
            }
        }
        return fileList;
    }

    /**
     * 生成 svn changelog 文件
     *
     * @param setting
     */
    private static void buildSvnChangelogFile(Setting setting) throws Exception {
        String command;
        String result;
        if (OSUtils.isWindows()) {
            if ((setting.getUsername() == null || "".equals(setting.getUsername()))
                    && (setting.getPassword() == null || "".equals(setting.getPassword()))) {
                command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_FOR_WINDOWS,
                        buildSvnVersionNumberParams(setting.getVersionNumbers().split(",")),
                        setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
            } else {
                command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_WINDOWS,
                        setting.getUsername(), setting.getPassword(),
                        buildSvnVersionNumberParams(setting.getVersionNumbers().split(",")),
                        setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
            }

            CommandUtils.exec(command);
        } else if (OSUtils.isLinux()) {
            if ((setting.getUsername() == null || "".equals(setting.getUsername()))
                    && (setting.getPassword() == null || "".equals(setting.getPassword()))) {
                command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_FOR_LINUX,
                        buildSvnVersionNumberParams(setting.getVersionNumbers().split(",")),
                        setting.getSvnRepositoryURL());
            } else {
                command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_LINUX,
                        setting.getUsername(), setting.getPassword(),
                        buildSvnVersionNumberParams(setting.getVersionNumbers().split(",")),
                        setting.getSvnRepositoryURL());
            }

            result = CommandUtils.exec(command);
            // 写 xml 文件
            IoUtil.write(new FileOutputStream(setting.getSvnChangelogOutPath()), "GBK", true, result);
        } else if (OSUtils.isMacOS() || OSUtils.isMacOSX()) {
            if ((setting.getUsername() == null || "".equals(setting.getUsername()))
                    && (setting.getPassword() == null || "".equals(setting.getPassword()))) {
                command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_FOR_SHELL,
                        buildSvnVersionNumberParams(setting.getVersionNumbers().split(",")),
                        setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
            } else {
                command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_SHELL,
                        setting.getUsername(), setting.getPassword(),
                        buildSvnVersionNumberParams(setting.getVersionNumbers().split(",")),
                        setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
            }
            // 生成 shell 文件
            buildShellFile(command);
            // 执行 shell 文件
            CommandUtils.exec("/bin/sh ./svn-log.sh");
            // 删除 shell 文件
            deleteFile("svn-log.sh");
        }
    }

    /**
     * 生成 shell 文件
     *
     * @param command
     * @throws FileNotFoundException
     */
    private static void buildShellFile(String command) throws FileNotFoundException {
        // 写 shell 文件
        String head = "#!/bin/sh";
        if (OSUtils.isMacOS()) {
            head += "\r";
        } else if (OSUtils.isMacOSX()) {
            head += "\n";
        }
        IoUtil.write(new FileOutputStream("svn-log.sh"), "UTF-8", true, head + command);
    }

    /**
     * 解析 svn changelog 文件
     *
     * @param filepath
     * @return
     */
    private static List<LogEntry> parseXml(String filepath) {
        Document document = XmlUtil.readXML(filepath);
        Element root = XmlUtil.getRootElement(document);
        List<LogEntry> logList = new ArrayList<>();

        if ("log".equals(root.getTagName())) {
            List<Element> logEntryList = XmlUtil.getElements(root, "logEntry".toLowerCase());
            for (Element logEntry : logEntryList) {
                String revision = logEntry.getAttribute("revision");
                Element authorElement = XmlUtil.getElement(logEntry, "author");
                Element dateElement = XmlUtil.getElement(logEntry, "date");
                Element msgElement = XmlUtil.getElement(logEntry, "msg");
                Element pathsElement = XmlUtil.getElement(logEntry, "paths");
                List<Element> pathElements = XmlUtil.getElements(pathsElement, "path");

                // 组装 logEntry
                LogEntry entry = new LogEntry();
                entry.setRevision(revision);
                entry.setAuthor(authorElement.getTextContent());
                entry.setDate(dateElement.getTextContent());
                entry.setMsg(msgElement.getTextContent());
                List<LogEntry.Path> pathList = new ArrayList<>();

                for (Element pathElement : pathElements) {
                    String action = pathElement.getAttribute("action");
                    String kind = pathElement.getAttribute("kind");
                    String path = pathElement.getTextContent();

                    // 组装 Path
                    LogEntry.Path pathEntry = new LogEntry.Path();
                    pathEntry.setKind(kind);
                    pathEntry.setAction(action);
                    pathEntry.setPath(path);
                    pathList.add(pathEntry);
                }
                entry.setPaths(pathList);
                logList.add(entry);
            }
        }

        return logList;
    }

    /**
     * 获取配置
     *
     * @return
     */
    private static Setting getDefaultSetting(Setting userSetting) {
        InputStream input = ClassUtils.getDefaultClassLoader().getResourceAsStream("setting.yml");
        Setting defaultSetting = YmlUtils.loadYml(input, Setting.class);
        userSetting = postProcessSetting(userSetting, defaultSetting);
        return userSetting;
    }

    /**
     * 后置处理配置
     *
     * @param userSetting
     * @param defaultSetting
     * @return
     */
    private static Setting postProcessSetting(Setting userSetting, Setting defaultSetting) {
        if (defaultSetting.getSvnChangelogOutPath() == null || DEFAULT_OUT_PATH.equals(defaultSetting.getSvnChangelogOutPath())) {
            defaultSetting.setSvnChangelogOutPath(System.getProperty("user.home") + File.separator + "changelog.xml");
        }
        if (userSetting.getSvnChangelogOutPath() == null) {
            userSetting.setSvnChangelogOutPath(defaultSetting.getSvnChangelogOutPath());
        }

        userSetting.setCompiledProjectDir(postProcessDir(userSetting.getCompiledProjectDir()));
        userSetting.setTargetUpdatePackageDir(postProcessDir(userSetting.getTargetUpdatePackageDir()));

        return userSetting;
    }

    /**
     * 生成 svn 版本查看日志的版本参数
     *
     * @param numbers 版本参数数组
     * @return
     */
    private static String buildSvnVersionNumberParams(String[] numbers) {
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(numbers).forEach(number -> stringBuilder.append(" -r " + number));
        return stringBuilder.toString();
    }
}
