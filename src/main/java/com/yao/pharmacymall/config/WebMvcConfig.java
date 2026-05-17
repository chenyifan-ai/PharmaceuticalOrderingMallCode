package com.yao.pharmacymall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolute = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        if (!absolute.endsWith(File.separator)) {
            absolute += File.separator;
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolute);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/sendCode",
                        "/api/product/list",
                        "/api/product/detail/**",
                        "/api/category/tree",
                        "/api/product/search",
                        "/api/c/product/list",
                        "/api/c/product/detail/**",
                        "/api/c/product/categories",
                        "/api/c/product/category-tree",
                        "/api/c/product/hot-searches",
                        "/api/c/product/search-suggestions",
                        "/api/c/product/recommend",
                        "/api/c/product/hot",
                        "/api/c/home",
                        "/api/c/home/**",
                        "/api/address/regions",
                        "/api/user/resetPassword",
                        "/api/payment/callback/success",
                        "/api/payment/callback/failure"
                );
    }
}
