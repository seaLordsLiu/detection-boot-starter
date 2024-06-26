package org.sealord.detection.autoconfigure;

import org.sealord.Configuration;
import org.sealord.config.TroubleConfig;
import org.sealord.detection.autoconfigure.properties.DetectionProperties;
import org.sealord.detection.autoconfigure.properties.TroubleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;


/**
 * detection自动装配程序
 * @author liu xw
 * @since 2024 04-20
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
    @ConditionalOnBean(CachingContentFilter.ContentWrapper.class)
    public FilterRegistrationBean<CachingContentFilter> requestBodyCacheFilter(@Nullable CachingContentFilter.ContentWrapper cw) {
        CachingContentFilter filter = new CachingContentFilter(cw);
        return new FilterRegistrationBean<>(filter);
    }

    @Bean(BEAN_CONFIGURATION)
    public Configuration configuration(DetectionProperties detectionProperties, ApplicationContext context) throws IllegalAccessException {
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
        log.info("Detection 服务初始化成功, applicationName: {}. evnLabel: {}", configuration.getApplicationName(), configuration.getEvnLabel());
        return configuration;
    }

    /**
     * 构造配置中心
     */
    private Configuration buildConfiguration(DetectionProperties properties) throws IllegalAccessException {
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
        return new TroubleConfig();
    }


}
