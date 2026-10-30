package com.oxyn.notes_api.note.dto.request;

import java.time.Instant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class NoteRequestDto {

  @NotBlank
  @Size(max = 150)
  private String site;

  @NotBlank
  @Size(max = 100)
  private String equipment;

  @NotBlank
  @Size(max = 100)
  private String variable;

  @NotNull
  private Instant timestamp;

  @NotBlank
  @Size(max = 150)
  private String author;

  @NotBlank
  private String message;
}
