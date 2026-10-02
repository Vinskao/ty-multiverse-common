package tw.com.ty.common.exception.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import tw.com.ty.common.exception.translate.ExceptionTranslator;

class CommonExceptionAutoConfigurationTest {

    private static final AutoConfigurations CONFIG = AutoConfigurations.of(CommonExceptionAutoConfiguration.class);

    @Test
    void servletApp_Should_GetServletAdviceOnly() {
        new WebApplicationContextRunner().withConfiguration(CONFIG).run(ctx -> {
            assertThat(ctx).hasSingleBean(ExceptionTranslator.class);
            assertThat(ctx).hasSingleBean(ServletExceptionAdvice.class);
            assertThat(ctx).doesNotHaveBean(ReactiveExceptionAdvice.class);
        });
    }

    @Test
    void reactiveApp_Should_GetReactiveAdviceOnly() {
        new ReactiveWebApplicationContextRunner().withConfiguration(CONFIG).run(ctx -> {
            assertThat(ctx).hasSingleBean(ExceptionTranslator.class);
            assertThat(ctx).hasSingleBean(ReactiveExceptionAdvice.class);
            assertThat(ctx).doesNotHaveBean(ServletExceptionAdvice.class);
        });
    }

    @Test
    void nonWebApp_Should_GetNoAdvice() {
        new ApplicationContextRunner().withConfiguration(CONFIG).run(ctx -> {
            assertThat(ctx).doesNotHaveBean(ServletExceptionAdvice.class);
            assertThat(ctx).doesNotHaveBean(ReactiveExceptionAdvice.class);
        });
    }

    @Test
    void disabledByProperty_Should_RegisterNothing() {
        new WebApplicationContextRunner().withConfiguration(CONFIG)
                .withPropertyValues("ty.common.exception.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(ServletExceptionAdvice.class));
    }

    @Test
    void application_Should_BeAbleToReplaceTheAdvice() {
        new WebApplicationContextRunner().withConfiguration(CONFIG)
                .withBean("custom", ServletExceptionAdvice.class, () -> new ServletExceptionAdvice(ExceptionTranslator.withDefaults()))
                .run(ctx -> assertThat(ctx).hasSingleBean(ServletExceptionAdvice.class));
    }

    @Test
    void serverErrorDetail_Should_BeHiddenByDefault() {
        new WebApplicationContextRunner().withConfiguration(CONFIG).run(ctx -> assertThat(
                ctx.getBean(ExceptionTranslator.class).translate(new IllegalStateException("kaboom"), "/x")
                        .body().getDetail()).doesNotContain("kaboom"));
    }

    @Test
    void serverErrorDetail_Should_BeExposedMasked_When_PropertyEnabled() {
        new WebApplicationContextRunner().withConfiguration(CONFIG)
                .withPropertyValues("ty.common.exception.expose-server-error-detail=true")
                .run(ctx -> assertThat(ctx.getBean(ExceptionTranslator.class)
                        .translate(new IllegalStateException("kaboom password=hunter2"), "/x").body().getDetail())
                        .contains("kaboom").doesNotContain("hunter2"));
    }

    @Test
    void autoConfiguration_Should_BeRegisteredForSpringBoot() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")) {
            assertThat(in).isNotNull();
            assertThat(new String(in.readAllBytes())).contains(CommonExceptionAutoConfiguration.class.getName());
        }
    }
}
