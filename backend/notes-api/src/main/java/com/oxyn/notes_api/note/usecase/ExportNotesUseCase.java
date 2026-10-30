package com.oxyn.notes_api.note.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.oxyn.notes_api.note.entity.Note;
import com.oxyn.notes_api.note.repository.NoteRepository;
import com.oxyn.notes_api.note.specification.NoteSpecification;
import com.oxyn.notes_api.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExportNotesUseCase {
  private final NoteRepository noteRepository;

  public String execute(String site, String equipment, LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new BadRequestException("endDate must be equal to or after startDate");
    }
    List<Note> notes = noteRepository.findAll(NoteSpecification.withFilters(site, equipment, startDate, endDate),
        Sort.by(Sort.Direction.DESC, "timestamp"));
    StringBuilder csv = new StringBuilder("\uFEFFid,site,equipment,variable,timestamp,author,message\r\n");
    for (Note note : notes) csv.append(row(note)).append("\r\n");
    return csv.toString();
  }

  private String row(Note note) {
    return String.join(",", cell(note.getId().toString()), cell(note.getSite()), cell(note.getEquipment()),
        cell(note.getVariable()), cell(note.getTimestamp().toString()), cell(note.getAuthor()), cell(note.getMessage()));
  }

  private String cell(String value) { return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\""; }
}
