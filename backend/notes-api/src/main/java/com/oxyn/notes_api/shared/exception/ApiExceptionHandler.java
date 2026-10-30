package com.oxyn.notes_api.shared.exception;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

import com.oxyn.notes_api.shared.dto.ApiErrorDto;
import com.oxyn.notes_api.shared.dto.FieldErrorDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorDto> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<FieldErrorDto> fields = exception.getBindingResult().getFieldErrors().stream()
        .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
        .distinct()
        .sorted(Comparator.comparing(FieldErrorDto::field).thenComparing(FieldErrorDto::message))
        .toList();
    return response(HttpStatus.BAD_REQUEST, "Dados inválidos", request, fields);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorDto> handleNotFound(
      ResourceNotFoundException exception, HttpServletRequest request) {
    return response(HttpStatus.NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiErrorDto> handleBadRequest(
      BadRequestException exception, HttpServletRequest request) {
    return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrorDto> handleStatus(
      ResponseStatusException exception, HttpServletRequest request) {
    return response(exception.getStatusCode().value(),
        HttpStatus.valueOf(exception.getStatusCode().value()).getReasonPhrase(),
        exception.getReason() == null ? "Requisição inválida" : exception.getReason(), request);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorDto> handleUnknownRoute(
      NoResourceFoundException exception, HttpServletRequest request) {
    return response(HttpStatus.NOT_FOUND, "Endpoint não encontrado", request);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiErrorDto> handleUnsupportedMethod(
      HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
    return response(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido", request);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiErrorDto> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
    return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type não suportado", request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorDto> handleParameterType(
      MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
    return response(HttpStatus.BAD_REQUEST,
        "Parâmetro inválido: " + exception.getName(), request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorDto> handleBody(
      HttpMessageNotReadableException exception, HttpServletRequest request) {
    return response(HttpStatus.BAD_REQUEST, "JSON inválido ou campo com formato incorreto", request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiErrorDto> handleMissingParameter(
      MissingServletRequestParameterException exception, HttpServletRequest request) {
    return response(HttpStatus.BAD_REQUEST,
        "Parâmetro obrigatório ausente: " + exception.getParameterName(), request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorDto> handleUnexpected(Exception exception, HttpServletRequest request) {
    log.error("Erro inesperado em {}", request.getRequestURI(), exception);
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", request);
  }

  private ResponseEntity<ApiErrorDto> response(
      HttpStatus status, String message, HttpServletRequest request) {
    return response(status, message, request, List.of());
  }

  private ResponseEntity<ApiErrorDto> response(
      HttpStatus status, String message, HttpServletRequest request, List<FieldErrorDto> fields) {
    return response(status.value(), status.getReasonPhrase(), message, request, fields);
  }

  private ResponseEntity<ApiErrorDto> response(
      int status, String error, String message, HttpServletRequest request) {
    return response(status, error, message, request, List.of());
  }

  private ResponseEntity<ApiErrorDto> response(
      int status, String error, String message, HttpServletRequest request, List<FieldErrorDto> fields) {
    return ResponseEntity.status(status).body(new ApiErrorDto(
        Instant.now(), status, error, message, request.getRequestURI(), fields));
  }
}
