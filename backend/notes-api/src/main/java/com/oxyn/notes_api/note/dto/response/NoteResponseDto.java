package com.oxyn.notes_api.note.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;

@Data 
public class NoteResponseDto {

  private UUID id;

  private String site;

  private String equipment;

  private String variable;

  private Instant timestamp;

  private String author;

  private String message;
}
