package xyz.xcye.file.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import xyz.xcye.web.common.exception.CustomGlobalExceptionHandler;
import xyz.xcye.web.common.manager.aop.GlobalLogAop;
import xyz.xcye.web.common.manager.advice.ResponseResultHandler;

import java.lang.reflect.Field;
import java.util.List;
//import xyz.xcye.interceptor.FileGlobalHandlerInterceptor;

/**
 * @author qsyyke
 */

@Configuration
public class FileWebConfig {
    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            @SuppressWarnings({"NullableProblems", "unchecked"})
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    try {
                        Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                        if (null != field) {
                            field.setAccessible(true);
                            List<RequestMappingInfoHandlerMapping> mappings = (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                            mappings.removeIf(e -> null != e.getPatternParser());
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return bean;
            }
        };
    }

    /*@Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }*/

    /**
     * 自定义全局异常处理
     * @return
     */
    @Bean
    public CustomGlobalExceptionHandler customGlobalExceptionHandler() {
        return new CustomGlobalExceptionHandler();
    }

    /**
     * 响应结果封装
     * @return
     */
    @Bean
    public ResponseResultHandler responseResultHandler() {
        return new ResponseResultHandler();
    }

    @Bean
    public GlobalLogAop globalLogAop() {
        return new GlobalLogAop();
    }
}
