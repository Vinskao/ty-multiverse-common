package tw.com.ty.common.exception.handler.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import tw.com.ty.common.exception.handler.ApiExceptionHandler;
import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;

/**
 * Spring's own web exceptions. Anything implementing {@link org.springframework.web.ErrorResponse}
 * (missing parameter, 405, 415, ResponseStatusException, ServerWebInputException, ...) carries its status;
 * an unreadable JSON body is a 400.
 */
public class SpringWebApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof org.springframework.web.ErrorResponse || ex instanceof HttpMessageNotReadableException;
    }

    @Override
    public HttpStatus status(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatus resolved = HttpStatus.resolve(springError.getStatusCode().value());
            return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public ErrorResponse handle(Exception ex, String requestUri) {
        HttpStatus status = status(ex);
        String detail = ex instanceof HttpMessageNotReadableException ? "請求內容格式錯誤" : ex.getMessage();
        if (status == HttpStatus.BAD_REQUEST) {
            return ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, detail, requestUri);
        }
        if (status == HttpStatus.NOT_FOUND) {
            return ErrorResponse.fromErrorCode(ErrorCode.NOT_FOUND, detail, requestUri);
        }
        return new ErrorResponse(status.value(), status.getReasonPhrase(), detail, requestUri);
    }
}
