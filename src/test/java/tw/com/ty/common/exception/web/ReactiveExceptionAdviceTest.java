package tw.com.ty.common.exception.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ResponseStatusException;

import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.translate.ExceptionTranslator;
import tw.com.ty.common.response.ErrorCode;

class ReactiveExceptionAdviceTest {

    private final ReactiveExceptionAdvice advice = new ReactiveExceptionAdvice(ExceptionTranslator.withDefaults());

    private static ServerHttpRequest request(String path) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getPath()).thenReturn(RequestPath.parse(path, null));
        return request;
    }

    @Test
    void handle_Should_UseErrorCodeStatusAndPath_When_BusinessException() {
        var response = advice.handle(new BusinessException(ErrorCode.PEOPLE_NOT_FOUND, "no Bob"), request("/people/1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.PEOPLE_NOT_FOUND.getCode());
        assertThat(response.getBody().getDetail()).isEqualTo("no Bob");
        assertThat(response.getBody().getPath()).isEqualTo("/people/1");
    }

    @Test
    void handle_Should_KeepStatus_When_ServerWebInputStyleResponseStatusException() {
        var response = advice.handle(new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad"), request("/x"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handle_Should_Return500_When_Unexpected() {
        var response = advice.handle(new IllegalStateException("kaboom"), request("/x"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }
}
