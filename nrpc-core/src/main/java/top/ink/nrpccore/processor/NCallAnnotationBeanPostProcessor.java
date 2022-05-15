package top.ink.nrpccore.processor;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.ReflectionUtils;
import top.ink.nrpccore.anno.Ncall;
import top.ink.nrpccore.util.SpringBeanFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * desc: NCallAnnotationBeanPostProcessor
 *
 * @author ink
 * date:2022-05-14 11:03
 */
@Slf4j
public class NCallAnnotationBeanPostProcessor implements MergedBeanDefinitionPostProcessor, InstantiationAwareBeanPostProcessor {


    public static final String BEAN_NAME = "nCallAnnotationBeanPostProcessor";

    @Resource
    private ApplicationContext context;

    @PostConstruct
    public void init(){
        SpringBeanFactory springBeanFactory = (SpringBeanFactory) context.getBean("SpringBeanFactory");
        springBeanFactory.setApplicationContext(context);
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {

    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findInjectionMetadata(beanName,bean.getClass());
        if (metadata != null){
            try {
                metadata.inject(bean,beanName,pvs);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return pvs;
    }



    private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz) {
        Collection<NCallAnnotationBeanPostProcessor.AnnotatedFieldElement> fieldElements = findFieldAnnotationMetadata(clazz);
        return fieldElements.size() > 0 ? new AnnotatedInjectionMetadata(clazz,combine(fieldElements)) : null;
    }

    private Collection<AnnotatedFieldElement> findFieldAnnotationMetadata(Class<?> clazz) {
        final List<NCallAnnotationBeanPostProcessor.AnnotatedFieldElement> elements = new LinkedList<>();

        ReflectionUtils.doWithFields(clazz, field -> {
            if (field.isAnnotationPresent(Ncall.class)){
                elements.add(new AnnotatedFieldElement(field));
            }
        });
        return elements;
    }

    class AnnotatedInjectionMetadata extends InjectionMetadata {

        private final Collection<InjectedElement> fieldElements;


        public AnnotatedInjectionMetadata(Class<?> targetClass, Collection<InjectedElement> fieldElements) {
            super(targetClass, fieldElements);
            this.fieldElements = fieldElements;
        }



        public Collection<InjectedElement> getFieldElements() {
            return fieldElements;
        }

    }

    class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;

        protected AnnotatedFieldElement(Field field) {
            super(field, null);
            this.field = field;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            String serviceName = field.getAnnotation(Ncall.class).ServiceName();
            if (!NrpcProxy.SERVICE_NAME_MAP_CHANNEL.containsKey(serviceName)){
                Channel channel = NrpcProxy.initChannel(serviceName);
                NrpcProxy.SERVICE_NAME_MAP_CHANNEL.put(serviceName,channel);
                NrpcProxy.CHANNEL_MAP_SERVICE_NAME.put(channel,serviceName);
            }
            Object o = NrpcProxy.getProxy(field);
            ReflectionUtils.makeAccessible(field);
            field.set(bean,o);
        }

    }

    private static <T> Collection<T> combine(Collection<? extends T>... elements) {
        List<T> allElements = new ArrayList<>();
        for (Collection<? extends T> e : elements) {
            allElements.addAll(e);
        }
        return allElements;
    }
}
