package com.oxyn.notes_api.note.usecase;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.oxyn.notes_api.shared.exception.ResourceNotFoundException;
import com.oxyn.notes_api.note.dto.request.NoteRequestDto;
import com.oxyn.notes_api.note.dto.response.NoteResponseDto;
import com.oxyn.notes_api.note.entity.Note;
import com.oxyn.notes_api.note.mapper.NoteMapper;
import com.oxyn.notes_api.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateNoteUseCase {
  private final NoteRepository noteRepository;
  private final NoteMapper noteMapper;

  public NoteResponseDto execute(UUID id, NoteRequestDto request) {
    Note current = noteRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Nota não encontrada: " + id));
    return noteMapper.toResponse(noteRepository.save(noteMapper.toEntity(request, current)));
  }
}
