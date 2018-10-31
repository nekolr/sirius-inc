package com.nekolr.util;

import java.io.*;

/**
 * 命令行工具
 *
 * @author nekolr
 */
public class CommandUtils {

    private CommandUtils() {
    }

    /**
     * 执行命令，返回执行结果
     *
     * @param command
     * @return
     */
    public static String exec(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder(StringUtils.split(command, " "));
        // 合并标准输出和标准错误流
        processBuilder.redirectErrorStream(true);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String result;
            Process process = processBuilder.start();
            InputStream input = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "GBK"));
            try {
                // 阻塞等待执行完毕
                process.waitFor();
                while ((result = reader.readLine()) != null) {
                    stringBuilder.append(result);
                }
                // 执行失败
                if (process.exitValue() != 0) {
                    throw new RuntimeException(stringBuilder.toString());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
