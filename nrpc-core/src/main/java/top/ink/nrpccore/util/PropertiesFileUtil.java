package top.ink.nrpccore.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * desc: 配置文件工具类
 *
 * @author ink
 * date:2022-05-29 21:20
 */
@Slf4j
public class PropertiesFileUtil {

    private static final Properties PROPERTIES = new Properties();

    private PropertiesFileUtil() {
    }

     static {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + "application.properties";
        }
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            PROPERTIES.load(inputStreamReader);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", "application.properties");
        }

    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }


}
