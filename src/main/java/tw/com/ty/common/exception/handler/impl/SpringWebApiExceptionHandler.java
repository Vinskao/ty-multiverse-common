package tw.com.ty.common.exception.handler.impl;

import org.springframework.http.HttpStatus;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;

import tw.com.ty.common.exception.handler.ApiExceptionHandler;
import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;

/**
 * Spring's own web exceptions. Anything implementing {@link org.springframework.web.ErrorResponse}
 * (missing parameter, 405, 415, ResponseStatusException, ServerWebInputException, ...) carries its status;
 * unreadable JSON, a parameter of the wrong type, a failed binding or a Bean Validation constraint violation is a 400
 * (same as Spring's DefaultHandlerExceptionResolver). ConversionNotSupportedException is a server bug and stays 500.
 */
public class SpringWebApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof org.springframework.web.ErrorResponse || isBadInput(ex);
    }

    private static boolean isBadInput(Exception ex) {
        if (ex instanceof ConversionNotSupportedException) {
            return false;
        }
        return ex instanceof HttpMessageNotReadableException
                || ex instanceof TypeMismatchException
                || ex instanceof BindException
                || ThrowableTypes.isInstanceOf(ex, "jakarta.validation.ConstraintViolationException");
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
