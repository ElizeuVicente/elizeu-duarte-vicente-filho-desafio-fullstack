package com.oxyn.notes_api.note.specification;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.data.jpa.domain.Specification;

import com.oxyn.notes_api.note.entity.Note;

/** Builds composable database filters for note searches. */
public final class NoteSpecification {
  private NoteSpecification() {
  }

  public static Specification<Note> withFilters(String site, String equipment, LocalDate startDate,
      LocalDate endDate) {
    return Specification.where(hasSite(site))
        .and(hasEquipment(equipment))
        .and(onOrAfter(startDate))
        .and(beforeNextDay(endDate));
  }

  private static Specification<Note> hasSite(String site) {
    return (root, query, builder) -> hasText(site)
        ? builder.equal(root.get("site"), site.trim()) : null;
  }

  private static Specification<Note> hasEquipment(String equipment) {
    return (root, query, builder) -> hasText(equipment)
        ? builder.equal(root.get("equipment"), equipment.trim()) : null;
  }

  private static Specification<Note> onOrAfter(LocalDate startDate) {
    return (root, query, builder) -> startDate == null ? null
        : builder.greaterThanOrEqualTo(root.get("timestamp"), startDate.atStartOfDay().toInstant(ZoneOffset.UTC));
  }

  private static Specification<Note> beforeNextDay(LocalDate endDate) {
    return (root, query, builder) -> endDate == null ? null
        : builder.lessThan(root.get("timestamp"), endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
