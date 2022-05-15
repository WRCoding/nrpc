package top.ink.nrpccore.anno;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.processor.NCallRegistrar;

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
public @interface Ncall {

    /** 要注册的服务类名*/
    String ServiceName();
}
