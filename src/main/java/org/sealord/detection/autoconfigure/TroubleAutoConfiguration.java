package org.sealord.detection.autoconfigure;

import org.apache.commons.collections4.CollectionUtils;
import org.sealord.detection.autoconfigure.trouble.ExceptionIncidentsRemoteDelegateExceptionResolver;
import org.sealord.trouble.DefaultTroubleManage;
import org.sealord.trouble.SimpleTroubleContentGenerator;
import org.sealord.trouble.TroubleManage;
import org.sealord.trouble.delegate.HttpTroubleDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liu xw
 * @since 2024 04-26
 */
@AutoConfiguration(after = DetectionAutoConfiguration.class)
public class TroubleAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TroubleAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(TroubleManage.class)
    public TroubleManage troubleManage(){
        // 需要先加载过滤器的执行策略
        return this.defaultTroubleManage();
    }

    @Bean
    public ApplicationListener<ContextRefreshedEvent> troubleHandlerExceptionResolverCompositeApplication(TroubleManage troubleManage){
        return new TroubleHandlerExceptionResolverCompositeApplication(troubleManage);
    }

    /**
     * 构造 TroubleManage 管理中心
     * @return 结果
     */
    private TroubleManage defaultTroubleManage(){
        SimpleTroubleContentGenerator troubleContentGenerator = new SimpleTroubleContentGenerator();
        HttpTroubleDelegate troubleDelegate = new HttpTroubleDelegate();
        return new DefaultTroubleManage(troubleContentGenerator, troubleDelegate, Collections.emptyList());
    }

    /**
     * 配置容器加载完成后的监听事件
     */
    public static class TroubleHandlerExceptionResolverCompositeApplication implements ApplicationListener<ContextRefreshedEvent> {

        /**
         * 异常处理器
         */
        private final ExceptionIncidentsRemoteDelegateExceptionResolver exceptionResolver;

        public TroubleHandlerExceptionResolverCompositeApplication(TroubleManage troubleManage) {
            this.exceptionResolver = new ExceptionIncidentsRemoteDelegateExceptionResolver(troubleManage);
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            ApplicationContext context = event.getApplicationContext();
            // 异常处理器处理器
            HandlerExceptionResolverComposite handlerExceptionResolverComposite = context.getBean(HandlerExceptionResolverComposite.class);
            List<HandlerExceptionResolver> oldHandlerExceptionResolvers = handlerExceptionResolverComposite.getExceptionResolvers();

            // 配置上报的异常处理器
            List<HandlerExceptionResolver> newHandlerExceptionResolvers = new ArrayList<>(oldHandlerExceptionResolvers.size() + 1);
            // 自定义处理器
            newHandlerExceptionResolvers.add(exceptionResolver);
            if (CollectionUtils.isNotEmpty(oldHandlerExceptionResolvers)){
                newHandlerExceptionResolvers.addAll(oldHandlerExceptionResolvers);
            }
            handlerExceptionResolverComposite.setExceptionResolvers(newHandlerExceptionResolvers);
            newHandlerExceptionResolvers.stream().map(HandlerExceptionResolver::getClass).map(Class::getSimpleName)
                .forEach(name -> log.info("自定义异常解析器完成, 解析器信息: [{}]", name));
        }
    }

}