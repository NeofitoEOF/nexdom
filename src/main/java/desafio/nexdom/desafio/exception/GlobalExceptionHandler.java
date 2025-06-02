package desafio.nexdom.desafio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFoundException(
            ProductNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStockException(
            InsufficientStockException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request);
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String error, String message, WebRequest request) {
        Map<String, Object> body = ErrorResponseBuilder.create()
            .status(status)
            .error(error)
            .message(message)
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(body, status);
    }

    private static class ErrorResponseBuilder {
        private final Map<String, Object> body = new LinkedHashMap<>();
        public static ErrorResponseBuilder create() { return new ErrorResponseBuilder(); }
        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) { body.put("timestamp", timestamp); return this; }
        public ErrorResponseBuilder status(HttpStatus status) { body.put("status", status.value()); return this; }
        public ErrorResponseBuilder error(String error) { body.put("error", error); return this; }
        public ErrorResponseBuilder message(String message) { body.put("message", message); return this; }
        public ErrorResponseBuilder path(String path) { body.put("path", path); return this; }
        public Map<String, Object> build() { return body; }
    }
}
