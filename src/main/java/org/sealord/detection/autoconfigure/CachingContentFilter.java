package org.sealord.detection.autoconfigure;

import org.apache.hc.core5.http.HttpHeaders;
import org.sealord.HttpServletRequestBodyWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author liu xw
 * @since 2024 04-30
 */
public class CachingContentFilter extends OncePerRequestFilter {

    /**
     * z支持自定义数据
     */
    private final ContentWrapper cw;

    /**
     * 默认的wrapper处理累
     */
    public static final ContentWrapper DEFAULT_CONTENT_WRAPPER = DefaultHttpServletRequestBodyWrapper::new;

    public CachingContentFilter(ContentWrapper cw) {
        this.cw = cw;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (cw != null){
            // 当配置了cw当时候在
            filterChain.doFilter(cw.getWrapper(request), response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            return Boolean.TRUE;
        }
        // http请求
        return Boolean.FALSE;
    }

    @FunctionalInterface
    public interface ContentWrapper{

        /**
         * 返回 Wrapper 处理器
         * @param request 请求
         * @return 处理器
         */
        HttpServletRequestBodyWrapper getWrapper(HttpServletRequest request);
    }


}
