/**
 * 设计思路
 * 背景：在常用的 spring boot web 系统中，会在项目中通过{@link org.springframework.web.bind.annotation.ControllerAdvice}注册异常处理器，这样可以统一处理异常信息
 * 需求：补充前、后端交互异常监控，在执行 {@link org.springframework.web.bind.annotation.ControllerAdvice} 之前，把采集异常信息，并通过HTTP上报到数据平台
 *
 * 通过观察源码发现 {@link org.springframework.web.bind.annotation.ControllerAdvice} 最终会被解析到 {@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver.exceptionHandlerAdviceCache} 属性中
 * ExceptionHandlerAdviceCache 为一个Map对象信息
 *     - key：{@link org.springframework.web.bind.annotation.ControllerAdvice} 注解的类信息
 *     - Value：{@link org.springframework.web.method.annotation.ExceptionHandlerMethodResolver} 对象，该对象其实是 {@link org.springframework.web.bind.annotation.ExceptionHandler} 方法的代理对象
 *
 *
 *
 * 思路：Spring Bean 中只有 ExceptionHandlerExceptionResolver 对象信息，并没有注册 ExceptionHandlerMethodResolver 信息，所以无法直接通过Spring获取
 * 在容器启动完成后，获取 ExceptionHandlerExceptionResolver 对象，然后改造 exceptionHandlerAdviceCache 属性中的 ExceptionHandlerMethodResolver 对象信息
 *
 * 方案排除：
 * ExceptionHandlerMethodResolver 类的左右是提供处理异常信息的方法，如果代理该对象
 * 1. 业务边界干不强，尽管可以获取到异常信息，但是功能领域比较混乱
 * 2. 配置代理后需要在每个方法中添加上报逻辑，不符合开闭原则，实现起来也比较麻烦
 *
 * 思路2 在 spring boot 启动后，获取到 {@link org.springframework.web.bind.annotation.ControllerAdvice} 注解的Bean信息，并对其配置代理（只代理 {@link org.springframework.web.bind.annotation.ExceptionHandler} 方法）
 * 理由：因为需求本质上只是想拿到异常信息，但是看了内部组件并没有提供获取异常信息的方法的扩展点，如果代理内部组件出现问题修改、调整都比较麻烦。
 * 补充异常信息上报逻辑
 *
 *
 * 思路3 自定义 {@link org.springframework.web.servlet.HandlerExceptionResolver}组件信息，本质上每一个 {@link org.springframework.web.servlet.HandlerExceptionResolver}都是对异常信息的实现，所以可以自定义一个异常处理器
 * {@link org.springframework.web.bind.annotation.ControllerAdvice} 实现的 {@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver} 也是一个 {@link org.springframework.web.servlet.HandlerExceptionResolver}
 * 自定义的话，在功能划分上是最清晰的，也不会侵入其他业务组件
 * 注意：自定义的异常处理器需要在 {@link org.springframework.web.servlet.handler.HandlerExceptionResolverComposite} 中注册，并且需要在 {@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver} 之前执行
 * @author liu xw
 * @date 2024 04-20
 */
package org.sealord.detection.autoconfigure.trouble;