package org.sealord.detection.autoconfigure.properties;


import java.util.List;

/**
 * 故障参数设置
 * @author liu xw
 * @date 2024 04-20
 */
public class TroubleProperties {

    /**
     * 忽略的异常
     */
    private List<String> ignores;

    public List<String> getIgnores() {
        return ignores;
    }

    public void setIgnores(List<String> ignores) {
        this.ignores = ignores;
    }
}
