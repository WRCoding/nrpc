package top.ink.nrpccore.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * desc: 调用注册的服务注解
 *
 * @author ink
 * date:2022-05-12 22:16
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NCall {

    /** 要注册的服务类名*/
    String ServiceName();
}
