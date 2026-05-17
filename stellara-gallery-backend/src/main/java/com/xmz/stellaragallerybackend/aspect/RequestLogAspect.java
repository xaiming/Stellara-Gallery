package com.xmz.stellaragallerybackend.aspect;

import cn.hutool.core.date.StopWatch;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
/**
 * Controller 请求日志切面。
 */
public class RequestLogAspect {

    /**
     * 记录所有 controller 接口的请求方法、路径和耗时，方便开发阶段排查慢接口。
     */
    @Around("within(com.xmz.stellaragallerybackend.controller..*)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            // 放行原始接口逻辑，日志统计放到 finally，确保异常接口也能记录耗时。
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            if (request != null) {
                log.info("{} {} completed in {} ms", request.getMethod(), request.getRequestURI(), stopWatch.getTotalTimeMillis());
            }
        }
    }
}
