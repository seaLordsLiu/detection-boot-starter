package org.sealord.detection.autoconfigure.trouble;

import java.util.List;

/**
 * 配置类不太方便写
 * 补充一个接口扩展
 * @author liu xw
 * @date 2024 04-26
 */
@FunctionalInterface
public interface ExcludeExceptionFunction {

    List<Class<? extends Throwable>> exclude();
}
