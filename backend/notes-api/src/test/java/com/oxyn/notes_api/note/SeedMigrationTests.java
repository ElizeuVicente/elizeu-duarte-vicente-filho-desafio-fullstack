package com.oxyn.notes_api.note;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.oxyn.notes_api.note.repository.NoteRepository;

@SpringBootTest
class SeedMigrationTests {
  @Autowired
  private NoteRepository notes;

  @Test
  void importsOriginalIdsAndQuotedSites() {
    var note = notes.findById(UUID.fromString("6492c8f7-2a93-455f-9117-8f4aff988ec8"));
    assertThat(note).isPresent();
    assertThat(note.orElseThrow().getSite()).isEqualTo("Barros, Reis e Moraes");
    assertThat(notes.count()).isGreaterThanOrEqualTo(100);
  }
}
