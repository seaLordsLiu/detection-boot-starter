package org.sealord.detection.autoconfigure;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.sealord.config.Configuration;
import org.sealord.config.TroubleConfig;
import org.sealord.detection.autoconfigure.properties.DetectionProperties;
import org.sealord.detection.autoconfigure.properties.TroubleProperties;
import org.sealord.detection.autoconfigure.trouble.ExcludeExceptionFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * detection自动装配程序
 * @author liu xw
 * @date 2024 04-20
 */
@AutoConfiguration
@EnableConfigurationProperties(DetectionProperties.class)
public class DetectionAutoConfiguration {

    private final static Logger log = LoggerFactory.getLogger(DetectionAutoConfiguration.class);

    /**
     * configuration bean name
     */
    public static final String BEAN_CONFIGURATION = "detectionConfiguration";


    @Bean
    public FilterRegistrationBean<CachingContentFilter> requestBodyCacheFilter(@Nullable CachingContentFilter.ContentWrapper cw) {
        CachingContentFilter filter = new CachingContentFilter(cw);
        return new FilterRegistrationBean<>(filter);
    }

    @Bean(BEAN_CONFIGURATION)
    public Configuration configuration(DetectionProperties detectionProperties, ApplicationContext context){
        String applicationName = detectionProperties.getApplicationName();
        if (applicationName == null){
            // 如果没有名字的话, 默认填充 spring.application.name
            applicationName = context.getEnvironment().getProperty("spring.application.name");
        }
        if (applicationName == null){
            throw new IllegalArgumentException("properties org.sealord.application-name is null");
        }
        detectionProperties.setApplicationName(applicationName);

        // 构造配置中心
        Configuration configuration = buildConfiguration(detectionProperties);

        // 设置故障配置
        TroubleConfig troubleConfig = buildTroubleConfig(detectionProperties.getTrouble(), context);
        configuration.setTrouble(troubleConfig);

        return configuration;
    }

    /**
     * 构造配置中心
     */
    private Configuration buildConfiguration(DetectionProperties properties){
        Configuration configuration = new Configuration();
        configuration.setApplicationName(properties.getApplicationName());
        configuration.setEvnLabel(properties.getEnvLabel());
        return configuration;
    }

    /**
     * 构造故障配置
     * @param troubleProperties 构造故障配置
     * @param context 全局内容信息
     * @return 故障配置
     */
    private TroubleConfig buildTroubleConfig(TroubleProperties troubleProperties, ApplicationContext context){
        Set<Class<? extends Throwable>> excludes = new HashSet<>();
        if (Objects.nonNull(troubleProperties) && CollectionUtils.isNotEmpty(troubleProperties.getIgnores())) {
            log.debug("init exclude exception on properties: {}", troubleProperties.getIgnores());
            List<String> ignores = troubleProperties.getIgnores();
            for (String ignore : ignores) {
                try {
                    Class<?> cls = Class.forName(ignore);
                    if (!cls.isAssignableFrom(Throwable.class)){
                        log.error("properties exclude exception not is throwable. exception: {}. ", ignore);
                    }else {
                        excludes.add((Class<? extends Throwable>) cls);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("properties exclude exception init error. exception: {}. ", ignore, e);
                }
            }
        }

        // 获取配置的
        for (String beanName : context.getBeanNamesForType(ExcludeExceptionFunction.class)) {
            // 取到Bean信息
            ExcludeExceptionFunction te = context.getBean(beanName, ExcludeExceptionFunction.class);
            try {
                List<Class<? extends Throwable>> excludesOnFunction = te.exclude();
                for (Class<? extends Throwable> cls : excludesOnFunction) {
                    log.debug("trouble init bean exclude throwable: Bean: {}. excludes{}", beanName, cls.getName());
                    excludes.add(cls);
                }
            }catch (Exception e){
                log.error("trouble exclude throwable error: Bean: {}", beanName, e);
            }
        }
        TroubleConfig troubleConfig = new TroubleConfig();
        troubleConfig.setIgnoreError(ListUtils.unmodifiableList(new ArrayList<>(excludes)));
        return troubleConfig;
    }


}
