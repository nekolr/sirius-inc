package com.nekolr.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.XmlUtil;
import com.nekolr.model.LogEntry;
import com.nekolr.model.Result;
import com.nekolr.model.Setting;
import com.nekolr.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.nekolr.Constants.*;

/**
 * 主程序
 *
 * @author nekolr
 */
public class ApplicationService {
    /**
     * 逻辑入口
     *
     * @return 是否存在错误
     */
    public static boolean run(Setting userSetting) throws Exception {
        // 获取全局配置
        Setting setting = getDefaultSetting(userSetting);
        // 生成 svn changelog 文件
        generateSvnChangelogFile(setting);
        // 解析 svn changelog 文件
        List<LogEntry> logEntryList = parseXml(setting.getSvnChangelogOutPath());
        // 解析完毕后删除 changelog 文件
        FileUtils.deleteFile(setting.getSvnChangelogOutPath());
        // 收集所有的文件路径
        Result<List<String>> result = collectTargetFileList(logEntryList, setting.getCompiledProjectDir());
        // 生成更新包
        buildTargetUpdatePackage(setting.getTargetUpdatePackageDir(), result.getData(), setting.getCompiledProjectDir());
        // 打开更新包目录
        FileUtils.openDirectoryUnderWindows(setting.getTargetUpdatePackageDir());

        return result.isHasError();
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
                if (FileUtils.isDirectory(targetUpdatePackageDir + File.separator + filePath)) {
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
     * 收集所有的文件路径
     *
     * @param logEntries
     * @return
     */
    private static Result<List<String>> collectTargetFileList(List<LogEntry> logEntries, String compiledProjectDir) {
        List<String> filePathList = new ArrayList<>();
        Result<List<String>> result = new Result<>();
        if (logEntries != null) {
            for (LogEntry logEntry : logEntries) {
                List<LogEntry.Path> pathEntryList = logEntry.getPaths();
                for (LogEntry.Path pathEntry : pathEntryList) {
                    String targetFilePath = null;
                    String path = pathEntry.getPath();
                    // 如果是删除文件，则不做处理
                    if (!"D".equalsIgnoreCase(pathEntry.getAction())) {
                        if (path.indexOf(JAVA_SRC_PREFIX) != -1) {
                            targetFilePath = findJavaCompiledFile(path);
                            // 查找内部类
                            List<String> innerClassPaths = findInnerClassFiles(compiledProjectDir, targetFilePath);
                            if (innerClassPaths != null) {
                                filePathList.addAll(innerClassPaths);
                            }
                        } else if (path.indexOf(RESOURCE_PREFIX) != -1) {
                            targetFilePath = findResourceFile(path);
                        } else if (path.indexOf(OTHER_PREFIX) != -1) {
                            targetFilePath = findOtherFile(path);
                            // 测试源代码和测试资源文件不打包
                        } else if (path.indexOf(JAVA_TEST_SRC_PREFIX) != -1 || path.indexOf(RESOURCE_TEST_PREFIX) != -1) {
                            continue;
                            // POM 文件不打包
                        } else if (path.lastIndexOf(POM_FILE) != -1) {
                            continue;
                        } else {
                            // 写日志
                            LogUtils.write("svn 文件路径：[" + path + "] 没有找到对应的编译后文件");
                            result.setHasError(Boolean.TRUE);
                        }
                        filePathList.add(targetFilePath);
                    }
                }
            }
        }
        result.setData(filePathList);
        return result;
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
        if (!file.exists()) {
            throw new RuntimeException(file.getAbsolutePath() + " 文件不存在");
        }
        if (!file.isDirectory()) {
            String fileNameNoSuffix = file.getName().substring(0, file.getName().lastIndexOf("."));
            String prefix = classFilePath.substring(0, classFilePath.lastIndexOf(fileNameNoSuffix));
            File[] folderFiles = file.getParentFile().listFiles();
            // 查找并收集所有的内部类
            return Arrays.stream(folderFiles)
                    .filter(folder -> folder.getName().contains(fileNameNoSuffix + "$"))
                    .map(folder -> prefix + folder.getName())
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 生成 svn changelog 文件
     *
     * @param setting
     */
    private static void generateSvnChangelogFile(Setting setting) throws Exception {
        // 没有填写密码则执行不需要密码的命令
        if (StringUtils.isEmpty(setting.getUsername()) && StringUtils.isEmpty(setting.getPassword())) {
            if (OSUtils.isWindows()) {
                exec_svn_log_command_for_windows(setting, false);
            } else if (OSUtils.isLinux()) {
                exec_svn_log_command_for_linux(setting, false);
            } else if (OSUtils.isMacOS() || OSUtils.isMacOSX()) {
                exec_svn_log_command_for_shell(setting, false);
            }
        } else {
            if (OSUtils.isWindows()) {
                exec_svn_log_command_for_windows(setting, true);
            } else if (OSUtils.isLinux()) {
                exec_svn_log_command_for_linux(setting, true);
            } else if (OSUtils.isMacOS() || OSUtils.isMacOSX()) {
                exec_svn_log_command_for_shell(setting, true);
            }
        }
    }

    /**
     * 执行 windows 下对应的 svn 命令
     *
     * @param setting
     * @param hasPassword
     */
    private static void exec_svn_log_command_for_windows(Setting setting, boolean hasPassword) {
        String command;
        if (!hasPassword) {
            command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_FOR_WINDOWS,
                    buildSvnVersionNumberParams(setting.getVersionNumbers()),
                    setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
        } else {
            command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_WINDOWS,
                    setting.getUsername(), setting.getPassword(),
                    buildSvnVersionNumberParams(setting.getVersionNumbers()),
                    setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
        }
        CommandUtils.exec(command);
    }

    /**
     * 执行 Linux 下对应的 svn 命令
     *
     * @param setting
     * @param hasPassword
     */
    private static void exec_svn_log_command_for_linux(Setting setting, boolean hasPassword) throws FileNotFoundException {
        String command;
        if (!hasPassword) {
            command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_FOR_LINUX,
                    buildSvnVersionNumberParams(setting.getVersionNumbers()),
                    setting.getSvnRepositoryURL());
        } else {
            command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_LINUX,
                    setting.getUsername(), setting.getPassword(),
                    buildSvnVersionNumberParams(setting.getVersionNumbers()),
                    setting.getSvnRepositoryURL());
        }

        String result = CommandUtils.exec(command);
        // 写 xml 文件
        IoUtil.write(new FileOutputStream(setting.getSvnChangelogOutPath()), "GBK", true, result);
    }

    /**
     * 执行 Mac 下对应的 svn 命令
     *
     * @param setting
     * @param hasPassword
     */
    private static void exec_svn_log_command_for_shell(Setting setting, boolean hasPassword) throws FileNotFoundException {
        String command;
        if (!hasPassword) {
            command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_FOR_SHELL,
                    buildSvnVersionNumberParams(setting.getVersionNumbers()),
                    setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
        } else {
            command = MessageFormat.format(SVN_LOG_COMMAND_TEMPLATE_WITH_PASSWORD_FOR_SHELL,
                    setting.getUsername(), setting.getPassword(),
                    buildSvnVersionNumberParams(setting.getVersionNumbers()),
                    setting.getSvnRepositoryURL(), setting.getSvnChangelogOutPath());
        }

        // 生成 shell 文件
        buildShellFile(command);
        // 执行 shell 文件
        CommandUtils.exec("/bin/sh ./svn-log.sh");
        // 删除 shell 文件
        FileUtils.deleteFile("svn-log.sh");
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
     * 获取默认配置，并使用后置处理器合并用户配置
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

        userSetting.setCompiledProjectDir(FileUtils.processDirectoryPath(userSetting.getCompiledProjectDir()));
        userSetting.setTargetUpdatePackageDir(FileUtils.processDirectoryPath(userSetting.getTargetUpdatePackageDir()));

        return userSetting;
    }

    /**
     * 生成 svn 版本查看日志的版本参数
     *
     * @param versionNumbers 版本参数字符串
     * @return
     */
    private static String buildSvnVersionNumberParams(String versionNumbers) {
        String[] numbers = versionNumbers.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(numbers).forEach(number -> stringBuilder.append(" -r " + number));
        return stringBuilder.toString();
    }
}
