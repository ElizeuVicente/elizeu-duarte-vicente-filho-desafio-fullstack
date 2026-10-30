package com.oxyn.notes_api.note.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.oxyn.notes_api.note.dto.request.NoteRequestDto;
import com.oxyn.notes_api.note.dto.response.NoteResponseDto;
import com.oxyn.notes_api.note.usecase.CreateNoteUseCase;
import com.oxyn.notes_api.note.usecase.DeleteNoteUseCase;
import com.oxyn.notes_api.note.usecase.ExportNotesUseCase;
import com.oxyn.notes_api.note.usecase.GetNoteUseCase;
import com.oxyn.notes_api.note.usecase.ListNotesUseCase;
import com.oxyn.notes_api.note.usecase.UpdateNoteUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final CreateNoteUseCase createNoteUseCase;
    private final ListNotesUseCase listNotesUseCase;
    private final GetNoteUseCase getNoteUseCase;
    private final UpdateNoteUseCase updateNoteUseCase;
    private final DeleteNoteUseCase deleteNoteUseCase;
    private final ExportNotesUseCase exportNotesUseCase;

    public NoteResponseDto create(NoteRequestDto request) {
        return createNoteUseCase.execute(request);
    }

    public Page<NoteResponseDto> list(String site, String equipment, LocalDate startDate, LocalDate endDate, int page, int size) {
        return listNotesUseCase.execute(site, equipment, startDate, endDate, page, size);
    }

    public NoteResponseDto get(UUID id) {
        return getNoteUseCase.execute(id);
    }

    public NoteResponseDto update(UUID id, NoteRequestDto request) {
        return updateNoteUseCase.execute(id, request);
    }

    public void delete(UUID id) {
        deleteNoteUseCase.execute(id);
    }

    public String export(String site, String equipment, LocalDate startDate, LocalDate endDate) {
        return exportNotesUseCase.execute(site, equipment, startDate, endDate);
    }
}
