package com.oxyn.notes_api.note.mapper;

import org.springframework.stereotype.Component;

import com.oxyn.notes_api.note.dto.request.NoteRequestDto;
import com.oxyn.notes_api.note.dto.response.NoteResponseDto;
import com.oxyn.notes_api.note.entity.Note;

@Component
public class NoteMapper {
  public Note toEntity(NoteRequestDto request) {
    return Note.builder()
        .site(request.getSite())
        .equipment(request.getEquipment())
        .variable(request.getVariable())
        .timestamp(request.getTimestamp())
        .author(request.getAuthor())
        .message(request.getMessage())
        .build();
  }

  public Note toEntity(NoteRequestDto request, Note current) {
    return Note.builder()
        .id(current.getId())
        .site(request.getSite())
        .equipment(request.getEquipment())
        .variable(request.getVariable())
        .timestamp(request.getTimestamp())
        .author(request.getAuthor())
        .message(request.getMessage())
        .build();
  }

  public NoteResponseDto toResponse(Note note) {
    NoteResponseDto response = new NoteResponseDto();
    response.setId(note.getId());
    response.setSite(note.getSite());
    response.setEquipment(note.getEquipment());
    response.setVariable(note.getVariable());
    response.setTimestamp(note.getTimestamp());
    response.setAuthor(note.getAuthor());
    response.setMessage(note.getMessage());
    return response;
  }
}
