package top.ink.nrpccore.spi;

import com.sun.scenario.effect.impl.prism.PrImage;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.route.RouteHandle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 林北
 * @description
 * @date 2022-06-07 16:35
 */
@Slf4j
public class ExtensionLoad<T> {
    private static final String PREFIX = "META-INF/n-rpc/";

    private static final Map<String,Object> INSTANCE_MAP = new ConcurrentHashMap<>();

    private static final Map<String,ExtensionLoad<?>> EXTENSION_LOAD_MAP = new ConcurrentHashMap<>();

    private final Map<String, Class<?>> cacheClass = new ConcurrentHashMap<>();
    private Class<?> clazz;

    private ExtensionLoad(Class<?> clazz){
        this.clazz = clazz;
    }

    public static <T> ExtensionLoad<T> getExtensionLoader(Class<T> clazz){
        if (clazz == null){
            throw new IllegalArgumentException("clazz must not null");
        }
        ExtensionLoad<T> extensionLoad = (ExtensionLoad<T>) EXTENSION_LOAD_MAP.get(clazz.getName());
        if (extensionLoad == null){
            EXTENSION_LOAD_MAP.putIfAbsent(clazz.getName(), new ExtensionLoad<>(clazz));
            extensionLoad = (ExtensionLoad<T>) EXTENSION_LOAD_MAP.get(clazz.getName());
        }
        return extensionLoad;
    }

    public T getExtension(String name){
        T instance = null;
        try {
            Class<?> _clazz = getClasses().get(name);
            instance = (T) INSTANCE_MAP.get(_clazz.getName());
            if (instance == null){
                INSTANCE_MAP.putIfAbsent(_clazz.getName(), _clazz.newInstance());
                instance = (T) INSTANCE_MAP.get(_clazz.getName());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("getExtension error: {}", e.getMessage());
        }
        return instance;
    }

    private Map<String, Class<?>> getClasses() {
        if (cacheClass.size() == 0){
            loadClasses();
        }
        return cacheClass;
    }

    private void loadClasses() {
        try {
            String path = PREFIX + clazz.getName();
            Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources(path);
            while (urlEnumeration.hasMoreElements()){
                URL url = urlEnumeration.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
                        StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null){
                        String[] keyAndClass = line.split("=");
                        cacheClass.put(keyAndClass[0],Class.forName(keyAndClass[1]));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("loadClassed error: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        ExtensionLoad.getExtensionLoader(RouteHandle.class).getExtension("aa");
    }

}
