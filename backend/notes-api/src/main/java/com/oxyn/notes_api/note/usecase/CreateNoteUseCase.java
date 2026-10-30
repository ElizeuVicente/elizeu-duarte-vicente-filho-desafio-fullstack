package com.oxyn.notes_api.note.usecase;

import org.springframework.stereotype.Component;

import com.oxyn.notes_api.note.dto.request.NoteRequestDto;
import com.oxyn.notes_api.note.dto.response.NoteResponseDto;
import com.oxyn.notes_api.note.mapper.NoteMapper;
import com.oxyn.notes_api.note.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateNoteUseCase {
  private final NoteRepository noteRepository;
  private final NoteMapper noteMapper;

  public NoteResponseDto execute(NoteRequestDto request) {
    return noteMapper.toResponse(noteRepository.save(noteMapper.toEntity(request)));
  }
}
