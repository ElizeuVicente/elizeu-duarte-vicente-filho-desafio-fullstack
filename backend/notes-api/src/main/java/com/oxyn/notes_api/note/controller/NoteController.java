package com.oxyn.notes_api.note.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.oxyn.notes_api.note.dto.request.NoteRequestDto;
import com.oxyn.notes_api.note.dto.response.NoteResponseDto;
import com.oxyn.notes_api.note.service.NoteService;
import com.oxyn.notes_api.shared.exception.BadRequestException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Validated
public class NoteController {
  private final NoteService noteService;

  @PostMapping
  public ResponseEntity<NoteResponseDto> create(@Valid @RequestBody NoteRequestDto request) {
    NoteResponseDto note = noteService.create(request);
    return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(note.getId()).toUri()).body(note);
  }

  @GetMapping
  public Page<NoteResponseDto> list(
      @RequestParam(required = false) String site,
      @RequestParam(required = false) String equipment,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    if (page < 0 || size < 1 || size > 100) {
      throw new BadRequestException("page must be at least 0 and size must be between 1 and 100");
    }
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new BadRequestException("endDate must be equal to or after startDate");
    }
   
    return noteService.list(site, equipment, startDate, endDate, page, size);
  }

  @GetMapping(value = "/export", produces = "text/csv")
  public ResponseEntity<String> export(
      @RequestParam(required = false) String site,
      @RequestParam(required = false) String equipment,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new BadRequestException("endDate must be equal to or after startDate");
    }
    String filename = "notes-" + LocalDate.now() + ".csv";
    return ResponseEntity.ok().contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(noteService.export(site, equipment, startDate, endDate));
  }

  @GetMapping("/{id}")
  public NoteResponseDto getById(@PathVariable UUID id) {
    return noteService.get(id);
  }

  @PutMapping("/{id}")
  public NoteResponseDto update(@PathVariable UUID id, @Valid @RequestBody NoteRequestDto request) {
    return noteService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    noteService.delete(id);
    return ResponseEntity.noContent().build();
  }

}
