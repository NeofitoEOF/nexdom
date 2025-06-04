package desafio.nexdom.desafio.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

   
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFoundException(
            ProductNotFoundException ex, WebRequest request) {
        log.warn("Produto não encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Produto não encontrado", ex.getMessage(), request);
    }

   
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStockException(
            InsufficientStockException ex, WebRequest request) {
        log.warn("Estoque insuficiente: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Estoque insuficiente", ex.getMessage(), request);
    }
    
  
    @ExceptionHandler(InsufficientEntryStockForProfitException.class)
    public ResponseEntity<Object> handleInsufficientEntryStockForProfitException(
            InsufficientEntryStockForProfitException ex, WebRequest request) {
        log.warn("Estoque de entrada insuficiente para cálculo de lucro: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Erro no cálculo de lucro", ex.getMessage(), request);
    }
    
   
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        log.warn("Entidade não encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request);
    }
    
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        String message = "Violação de integridade de dados";
        
        if (ex.getCause() != null && ex.getCause().getMessage().contains("unique constraint")) {
            message = "Já existe um registro com os dados informados";
        }
        
        log.warn("Violação de integridade: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflito de dados", message, request);
    }
    
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message;
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null) {
            message = String.format("O parâmetro '%s' com valor '%s' não pôde ser convertido para o tipo %s", 
                ex.getName(), ex.getValue(), requiredType.getSimpleName());
        } else {
            message = String.format("O parâmetro '%s' com valor '%s' não pôde ser convertido para o tipo requerido", 
                ex.getName(), ex.getValue());
        }
        
        log.warn("Erro de conversão de tipo: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Erro de parâmetro", message, request);
    }
    
  
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        ApiError apiError = createApiError(HttpStatus.BAD_REQUEST, "Erro de validação", "Ocorreram erros de validação", request);
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.contains(".") ? 
                    propertyPath.substring(propertyPath.lastIndexOf(".") + 1) : propertyPath;
            apiError.addValidationError(field, violation.getMessage());
        }
        
        log.warn("Violações de constraints: {}", violations.size());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
    
   
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        log.error("Erro não tratado: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Erro interno do servidor", 
                "Ocorreu um erro inesperado. Por favor, contate o suporte.", 
                request);
    }
    
   
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex, 
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatusCode status, 
            @NonNull WebRequest request) {
        
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST, 
                "Erro de validação", 
                "Ocorreram erros de validação nos dados enviados", 
                request);
        
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        
        for (FieldError fieldError : fieldErrors) {
            apiError.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        log.warn("Erros de validação: {}", fieldErrors.size());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
    
    
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex, 
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatusCode status, 
            @NonNull WebRequest request) {
        
        log.warn("Requisição malformada: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, 
                "Requisição inválida", 
                "O corpo da requisição está malformado ou contém JSON inválido", 
                request);
    }
    
    
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            @NonNull NoHandlerFoundException ex, 
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatusCode status, 
            @NonNull WebRequest request) {
        
        String message = String.format("Não foi encontrado um manipulador para %s %s", 
                ex.getHttpMethod(), ex.getRequestURL());
        
        log.warn("Endpoint não encontrado: {}", ex.getRequestURL());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Endpoint não encontrado", message, request);
    }

    
    private ResponseEntity<Object> buildErrorResponse(
            HttpStatus status, String error, String message, WebRequest request) {
        
        ApiError apiError = createApiError(status, error, message, request);
        return new ResponseEntity<>(apiError, status);
    }
    
    
    private ApiError createApiError(
            HttpStatus status, String error, String message, WebRequest request) {
        
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        
        return ApiError.builder()
                .status(status)
                .statusCode(status.value())
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
