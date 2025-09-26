package myproject.booktalk.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.ui.Model;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@Order(0)
public class GlobalExceptionHandler {

    /* ===== 400 계열: 검증/파라미터 바인딩 오류 ===== */
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public String handleBindErrors(Exception ex, HttpServletRequest req, HttpServletResponse res, Model model) {
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        List<FieldErrorDetail> fieldErrors = (ex instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldErrorDetail.of(fe.getField(), fe.getCode(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .collect(Collectors.toList())
                : ((BindException) ex).getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldErrorDetail.of(fe.getField(), fe.getCode(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .collect(Collectors.toList());

        addCommon(model, HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.", req, fieldErrors);
        return "error/400";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req, HttpServletResponse res, Model model) {
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> FieldErrorDetail.of(v.getPropertyPath().toString(),
                        v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        v.getMessage(), v.getInvalidValue()))
                .collect(Collectors.toList());

        addCommon(model, HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.", req, fieldErrors);
        return "error/400";
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public String handleBadRequest(Exception ex, HttpServletRequest req, HttpServletResponse res, Model model) {
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        addCommon(model, HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
        return "error/400";
    }

    /* ===== 403 ===== */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleDenied(AccessDeniedException ex, HttpServletRequest req, HttpServletResponse res, Model model) {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        addCommon(model, HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", req, null);
        return "error/403";
    }

    /* ===== 405 ===== */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req, HttpServletResponse res, Model model) {
        res.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        addCommon(model, HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req, null);
        return "error/405";
    }

    /* ===== 명시적 상태 지정(ResponseStatusException) ===== */
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, HttpServletRequest req, HttpServletResponse res, Model model) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        res.setStatus(status.value());

        String view = pickViewByStatus(status);
        addCommon(model, status, ex.getReason() != null ? ex.getReason() : "요청 처리 중 오류가 발생했습니다.", req, null);
        return view;
    }

    /* ===== 그 외(500) ===== */
    @ExceptionHandler(Exception.class)
    public String handleAll(Exception ex, HttpServletRequest req, HttpServletResponse res, Model model) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        addCommon(model, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", req, null);
        return "error/500";
    }

    /* ===== 유틸 ===== */
    private void addCommon(Model model, HttpStatus status, String message, HttpServletRequest req, List<FieldErrorDetail> fieldErrors) {
        model.addAttribute("status", status.value());
        model.addAttribute("error", status.getReasonPhrase());
        model.addAttribute("message", message);
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("timestamp", OffsetDateTime.now());
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            model.addAttribute("fieldErrors", fieldErrors);
        }
    }

    private String pickViewByStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "error/400";
            case FORBIDDEN -> "error/403";
            case NOT_FOUND -> "error/404";
            case METHOD_NOT_ALLOWED -> "error/405";
            default -> status.is5xxServerError() ? "error/500" : "error/error";
        };
    }
}