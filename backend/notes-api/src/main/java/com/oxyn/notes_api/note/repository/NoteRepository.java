package com.oxyn.notes_api.note.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.oxyn.notes_api.note.entity.Note;

public interface NoteRepository extends JpaRepository<Note, UUID>, JpaSpecificationExecutor<Note> {

}
