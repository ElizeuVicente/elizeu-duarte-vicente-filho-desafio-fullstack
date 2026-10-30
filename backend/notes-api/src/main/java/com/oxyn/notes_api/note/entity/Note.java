package com.oxyn.notes_api.note.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "notes",
    indexes = {
      @Index(name = "idx_notes_site", columnList = "site"),
      @Index(name = "idx_notes_equipment", columnList = "equipment"),
      @Index(name = "idx_notes_timestamp", columnList = "note_timestamp")
    })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 150)
  private String site;

  @Column(nullable = false, length = 100)
  private String equipment;

  @Column(nullable = false, length = 100)
  private String variable;

  @Column(name = "note_timestamp", nullable = false)
  private Instant timestamp;

  @Column(nullable = false, length = 150)
  private String author;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;
}
