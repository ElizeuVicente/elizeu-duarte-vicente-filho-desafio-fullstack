package com.oxyn.notes_api.note.usecase;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import com.oxyn.notes_api.note.dto.response.NoteResponseDto;
import com.oxyn.notes_api.note.mapper.NoteMapper;
import com.oxyn.notes_api.note.entity.Note;
import com.oxyn.notes_api.note.repository.NoteRepository;
import com.oxyn.notes_api.note.specification.NoteSpecification;
import com.oxyn.notes_api.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ListNotesUseCase {
  private final NoteRepository noteRepository;
  private final NoteMapper noteMapper;

  public Page<NoteResponseDto> execute(String site, String equipment, LocalDate startDate,
      LocalDate endDate, int page, int size) {
    if (page < 0 || size < 1 || size > 100) {
      throw new BadRequestException(
          "page must be at least 0 and size must be between 1 and 100");
    }
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new BadRequestException(
          "endDate must be equal to or after startDate");
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

    Specification<Note> specification = NoteSpecification.withFilters(site, equipment, startDate, endDate);
    return noteRepository.findAll(specification, pageable).map(noteMapper::toResponse);
  }
}
