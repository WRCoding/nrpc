package top.ink.nrpccore.anno;

import org.springframework.context.annotation.Import;
import top.ink.nrpccore.processor.NCallRegistrar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * desc: EnableNrpc
 *
 * @author ink
 * date:2022-05-14 15:51
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(NCallRegistrar.class)
public @interface EnableNrpc {
}
