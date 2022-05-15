package top.ink.nrpccore.anno;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * desc: 注册服务注解
 *
 * @author ink
 * date:2022-05-12 22:07
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface NService {

    /** 要注册的服务类名*/
    String ServiceName();
}
