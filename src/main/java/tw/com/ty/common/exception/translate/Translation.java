package tw.com.ty.common.exception.translate;

import org.springframework.http.HttpStatus;

import tw.com.ty.common.response.ErrorResponse;

/** Framework-neutral result of translating an exception: the HTTP status plus the error payload. */
public record Translation(HttpStatus status, ErrorResponse body) {
}
