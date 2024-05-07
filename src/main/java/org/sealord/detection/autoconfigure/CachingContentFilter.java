package org.sealord.detection.autoconfigure;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.sealord.http.wrapper.DefaultContentCachingRequestWrapper;
import org.sealord.http.wrapper.PostBodyRequestWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author liu xw
 * @date 2024 04-30
 */
public class CachingContentFilter extends OncePerRequestFilter {

    /**
     * z支持自定义数据
     */
    private final ContentWrapper cw;

    public CachingContentFilter(ContentWrapper cw) {
        ContentWrapper DEFAULT_C_W = DefaultContentCachingRequestWrapper::new;
        this.cw = Objects.isNull(cw) ? DEFAULT_C_W : cw;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(cw.getWrapper(request), response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            return Boolean.TRUE;
        }
        // http请求
        return !contentType.startsWith(ContentType.APPLICATION_JSON.getMimeType());
    }

    @FunctionalInterface
    public interface ContentWrapper{

        /**
         * 返回 Wrapper 处理器
         * @param request 请求
         * @return 处理器
         */
        PostBodyRequestWrapper getWrapper(HttpServletRequest request);
    }
}
