package com.xmz.stellaragallerybackend.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * Sa-Token 登录拦截配置。
 */
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * Sa-Token 全局拦截器：除登录、注册、健康检查和接口文档外，其余接口都必须登录。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
                        // 公开接口无需登录，便于用户拿到登录态或查看服务健康状态。
                        .notMatch("/user/login", "/user/register")
                        .notMatch("/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}
