package top.ink.nrpccore.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * desc: 配置文件工具类
 *
 * @author ink
 * date:2022-06-06 22:17
 */
public class PropertiesUtil {

    private final static Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try (InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            PROPERTIES.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties getProperties(){
        return PROPERTIES;
    }

    public static String getValue(String key){
        return PROPERTIES.getProperty(key);
    }
}
