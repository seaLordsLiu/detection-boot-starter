package org.sealord.detection.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 参数设置
 * @author liu xw
 * @date 2024 04-20
 */
@ConfigurationProperties(prefix = "org.sealord.detection")
public class DetectionProperties {

    /**
     * 服务名称
     */
    private String applicationName;

    /**
     * 环境变量
     */
    private String envLabel;

    /**
     * 故障配置
     */
    private TroubleProperties trouble;



    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEnvLabel() {
        return envLabel;
    }

    public void setEnvLabel(String envLabel) {
        this.envLabel = envLabel;
    }

    public TroubleProperties getTrouble() {
        return trouble;
    }

    public void setTrouble(TroubleProperties trouble) {
        this.trouble = trouble;
    }
}
