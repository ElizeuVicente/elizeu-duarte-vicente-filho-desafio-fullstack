package com.oxyn.notes_api.shared.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorDto(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldErrorDto> fieldErrors) {
}
