package top.ink.nrpccore.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.anno.Ncall;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * desc: 客户端处理类
 *
 * @author ink
 * date:2022-05-14 09:51
 */
@Component
@Slf4j
public class CallProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        if (Arrays.stream(bean.getClass().getFields()).anyMatch(field -> field.isAnnotationPresent(Ncall.class))) {
//            Field[] declaredFields = bean.getClass().getDeclaredFields();
//            for (Field declaredField : declaredFields) {
//                log.info(declaredField.getType().toString());
//            }
//        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
