package tw.com.ty.common.exception.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tw.com.ty.common.exception.translate.ExceptionTranslator;

/**
 * Registers the shared error handling in any Spring Boot application that has this library on its
 * classpath: the right advice for the web stack (servlet MVC or reactive WebFlux), backed by one
 * {@link ExceptionTranslator}. 5xx details are hidden (masked errorId only) unless
 * {@code ty.common.exception.expose-server-error-detail=true}. Disable with {@code ty.common.exception.enabled=false}; define your own
 * {@link ExceptionTranslator} or advice bean to override.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "ty.common.exception", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CommonExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExceptionTranslator exceptionTranslator(
            @Value("${ty.common.exception.expose-server-error-detail:false}") boolean exposeServerErrorDetail) {
        return ExceptionTranslator.withDefaults(exposeServerErrorDetail);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    static class ServletConfiguration {
        @Bean
        @ConditionalOnMissingBean
        ServletExceptionAdvice servletExceptionAdvice(ExceptionTranslator translator) {
            return new ServletExceptionAdvice(translator);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    static class ReactiveConfiguration {
        @Bean
        @ConditionalOnMissingBean
        ReactiveExceptionAdvice reactiveExceptionAdvice(ExceptionTranslator translator) {
            return new ReactiveExceptionAdvice(translator);
        }
    }
}
