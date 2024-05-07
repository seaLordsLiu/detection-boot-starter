package org.sealord.detection.autoconfigure.trouble;

import lombok.Getter;
import org.sealord.client.trouble.TroubleClient;
import org.sealord.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理器
 * 自定义 {@link org.springframework.web.servlet.HandlerExceptionResolver}组件信息，本质上每一个 {@link org.springframework.web.servlet.HandlerExceptionResolver}都是对异常信息的实现，所以可以自定义一个异常处理器
 * {@link org.springframework.web.bind.annotation.ControllerAdvice} 实现的 {@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver} 也是一个 {@link org.springframework.web.servlet.HandlerExceptionResolver}
 * 自定义的话，在功能划分上是最清晰的，也不会侵入其他业务组件
 * 注意：自定义的异常处理器需要在 {@link org.springframework.web.servlet.handler.HandlerExceptionResolverComposite} 中注册，并且需要在 {@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver} 之前执行
 * @author liu xw
 * @date 2024 04-20
 */
public class ExceptionIncidentsRemoteDelegateExceptionResolver implements HandlerExceptionResolver {

    private final static Logger log = LoggerFactory.getLogger(ExceptionIncidentsRemoteDelegateExceptionResolver.class);

    /**
     * 配置信息
     */
    @Getter
    private final Configuration configuration;

    /**
     * 故障客户端
     */
    @Getter
    private final TroubleClient troubleClient;


    public ExceptionIncidentsRemoteDelegateExceptionResolver(Configuration configuration, TroubleClient troubleClient) {
        this.configuration = configuration;
        this.troubleClient = troubleClient;
    }

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 远程异常处理 - 最好可以有异步执行, 减少对主业务的影响
        try {
            troubleClient.reportTrouble(ex, request);
        }catch (Exception e){
            log.error("report trouble error. request url: {}", request.getRequestURI(), e);
        }
        // 必须要为NULL，才会被其他的异常处理器处理
        return null;
    }
}