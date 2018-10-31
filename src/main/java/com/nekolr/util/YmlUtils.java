package com.nekolr.util;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * YML 工具类
 *
 * @author nekolr
 */
public class YmlUtils {

    private static Yaml yaml = new Yaml();

    private YmlUtils() {
    }

    public static Yaml getYml() {
        return yaml;
    }

    public static <T> T loadYml(InputStream input, Class<T> type) {
        return yaml.loadAs(input, type);
    }
}
