package com.oxyn.notes_api.note.usecase;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.oxyn.notes_api.shared.exception.ResourceNotFoundException;
import com.oxyn.notes_api.note.dto.response.NoteResponseDto;
import com.oxyn.notes_api.note.mapper.NoteMapper;
import com.oxyn.notes_api.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetNoteUseCase {
  private final NoteRepository noteRepository;
  private final NoteMapper noteMapper;

  public NoteResponseDto execute(UUID id) {
    return noteMapper.toResponse(noteRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Nota não encontrada: " + id)));
  }
}
