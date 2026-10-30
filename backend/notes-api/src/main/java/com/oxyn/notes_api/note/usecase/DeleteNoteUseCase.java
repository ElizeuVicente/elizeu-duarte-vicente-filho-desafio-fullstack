package com.oxyn.notes_api.note.usecase;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.oxyn.notes_api.shared.exception.ResourceNotFoundException;
import com.oxyn.notes_api.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteNoteUseCase {
  private final NoteRepository noteRepository;

  public void execute(UUID id) {
    if (!noteRepository.existsById(id))
      throw new ResourceNotFoundException("Nota não encontrada: " + id);
    noteRepository.deleteById(id);
  }
}
