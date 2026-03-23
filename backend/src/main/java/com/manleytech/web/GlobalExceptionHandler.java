package com.manleytech.web;

import com.manleytech.constant.dify.ErrorCode;
import com.manleytech.entity.dify.resp.StandardResponse;
import com.manleytech.entity.dify.resp.DifyError;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolationException;

@Singleton
@Produces
public class GlobalExceptionHandler implements ExceptionHandler<Exception, HttpResponse<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public HttpResponse<?> handle(HttpRequest request, Exception exception) {
        LOG.error("Unhandled exception for request: {} {}", request.getMethod(), request.getPath(), exception);

        if (exception instanceof DifyController.UnauthorizedException) {
            DifyController.UnauthorizedException authException = (DifyController.UnauthorizedException) exception;
            return HttpResponse.status(HttpStatus.FORBIDDEN)
                    .body(authException.getError());
        }

        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception;
            String message = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation failed");
            return HttpResponse.badRequest(StandardResponse.error(ErrorCode.VALIDATION_ERROR, message));
        }

        if (exception instanceof IllegalArgumentException) {
            return HttpResponse.badRequest(StandardResponse.error(ErrorCode.VALIDATION_ERROR, exception.getMessage()));
        }

        if (exception instanceof ResourceNotFoundException) {
            return HttpResponse.notFound(StandardResponse.error(ErrorCode.RESOURCE_NOT_FOUND, exception.getMessage()));
        }

        return HttpResponse.serverError(StandardResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage()));
    }
}