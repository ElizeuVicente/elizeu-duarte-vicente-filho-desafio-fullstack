package com.oxyn.notes_api.note.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
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
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
   
    return noteService.list(site, equipment, startDate, endDate, page, size);
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
