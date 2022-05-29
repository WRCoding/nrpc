package top.ink.nrpccore.processor;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import top.ink.nrpccore.entity.RpcProperties;
import top.ink.nrpccore.route.LoopRouteHandle;
import top.ink.nrpccore.route.RandomRouteHandle;
import top.ink.nrpccore.util.SpringBeanFactory;

import javax.annotation.Resource;

/**
 * desc: NCallRegistrar
 *
 * @author ink
 * date:2022-05-14 15:37
 */
public class NCallRegistrar implements ImportBeanDefinitionRegistrar {


    @Resource
    private RpcProperties rpcProperties;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerNCallAnnotationBeanPostProcessor(registry);
    }

    private void registerNCallAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringBeanFactory.class);
        registry.registerBeanDefinition("SpringBeanFactory",beanDefinition);
        registerInfrastructureBean(registry,
                NCallAnnotationBeanPostProcessor.BEAN_NAME, NCallAnnotationBeanPostProcessor.class);
    }



    public void registerInfrastructureBean(BeanDefinitionRegistry beanDefinitionRegistry,
                                                  String beanName,
                                                  Class<?> beanType) {
        if (!beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(beanType);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
        }


    }
}
