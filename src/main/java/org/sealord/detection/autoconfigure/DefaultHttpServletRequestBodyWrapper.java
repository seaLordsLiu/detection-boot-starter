package org.sealord.detection.autoconfigure;

import org.sealord.HttpServletRequestBodyWrapper;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;

/**
 * 这个是一个默认
 * ContentCachingRequestWrapper 自由每次读取内存后才会进行缓存, 比较符合使用条件, 对其进行扩张补充
 * HttpServletRequestBodyWrapper 逻辑需求
 * @author liu xw
 * @since 2024 06-24
 */
public class DefaultHttpServletRequestBodyWrapper extends ContentCachingRequestWrapper implements HttpServletRequestBodyWrapper {

    public DefaultHttpServletRequestBodyWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public byte[] bodyInfo() {
        return super.getContentAsByteArray();
    }
}
