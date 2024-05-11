package org.sealord.detection.autoconfigure;

import org.apache.commons.collections4.CollectionUtils;
import org.sealord.client.trouble.TroubleClient;
import org.sealord.client.trouble.TroubleClientTemplate;
import org.sealord.config.Configuration;
import org.sealord.detection.autoconfigure.trouble.ExceptionIncidentsRemoteDelegateExceptionResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liu xw
 * @since 2024 04-26
 */
@AutoConfiguration
@AutoConfigureAfter(DetectionAutoConfiguration.class)
public class TroubleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TroubleClient.class)
    public TroubleClient troubleProperties(Configuration configuration){
        return new TroubleClientTemplate(configuration);
    }

    /**
     * web端检测
     * @param configuration 配置信息
     */
    @Bean
    @ConditionalOnBean(TroubleClient.class)
    public BeanPostProcessor troubleExceptionResolver(Configuration configuration, TroubleClient troubleClient){
        ExceptionIncidentsRemoteDelegateExceptionResolver exceptionResolver = new ExceptionIncidentsRemoteDelegateExceptionResolver(configuration, troubleClient);
        return new HandlerExceptionResolverBeanPostProcessor(exceptionResolver);
    }

    /**
     * 通过 WebMvcConfigurer 注入异常处理器
     */
    public static class HandlerExceptionResolverBeanPostProcessor implements BeanPostProcessor {

        /**
         * 异常处理器
         */
        @NonNull
        private final HandlerExceptionResolver extendHandlerExceptionResolver;

        public HandlerExceptionResolverBeanPostProcessor(@NonNull HandlerExceptionResolver extendHandlerExceptionResolver) {
            this.extendHandlerExceptionResolver = extendHandlerExceptionResolver;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof HandlerExceptionResolverComposite){
                // 异常处理器处理器
                HandlerExceptionResolverComposite composite = (HandlerExceptionResolverComposite) bean;
                List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();
                // 自定义处理器
                exceptionResolvers.add(extendHandlerExceptionResolver);
                // 添加原有处理器
                List<HandlerExceptionResolver> existExceptionResolvers = composite.getExceptionResolvers();
                if (CollectionUtils.isNotEmpty(existExceptionResolvers)){
                    exceptionResolvers.addAll(existExceptionResolvers);
                }
                composite.setExceptionResolvers(exceptionResolvers);
            }
            return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
        }
    }

}