package db.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Imports the challenge CSV once, as part of the same migration history as the schema. */
public class V2__seed_notes extends BaseJavaMigration {
  private static final String RESOURCE = "/seed/notes.csv";
  private static final List<String> HEADER = List.of("id", "site", "equipment", "variable", "timestamp", "author", "message");

  @Override
  public void migrate(Context context) throws Exception {
    InputStream source = getClass().getResourceAsStream(RESOURCE);
    if (source == null) throw new IllegalStateException("CSV de carga inicial não encontrado: " + RESOURCE);

    try (source;
        PushbackReader reader = new PushbackReader(new InputStreamReader(source, StandardCharsets.UTF_8), 1);
        PreparedStatement insert = context.getConnection().prepareStatement("""
            INSERT INTO notes (id, site, equipment, variable, note_timestamp, author, message)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO NOTHING
            """)) {
      List<List<String>> rows = readCsv(reader);
      if (rows.isEmpty() || !rows.get(0).equals(HEADER)) {
        throw new IllegalStateException("Cabeçalho inválido no CSV de carga inicial");
      }
      for (int index = 1; index < rows.size(); index++) {
        List<String> row = rows.get(index);
        if (row.size() != HEADER.size()) {
          throw new IllegalStateException("Quantidade de colunas inválida na linha " + (index + 1));
        }
        insert.setObject(1, UUID.fromString(row.get(0)));
        insert.setString(2, row.get(1));
        insert.setString(3, row.get(2));
        insert.setString(4, row.get(3));
        insert.setTimestamp(5, Timestamp.from(Instant.parse(row.get(4))));
        insert.setString(6, row.get(5));
        insert.setString(7, row.get(6));
        insert.addBatch();
      }
      insert.executeBatch();
    }
  }

  /** Handles quoted commas, escaped quotes and line breaks inside CSV fields. */
  private static List<List<String>> readCsv(PushbackReader reader) throws IOException {
    List<List<String>> rows = new ArrayList<>();
    List<String> row = new ArrayList<>();
    StringBuilder field = new StringBuilder();
    boolean quoted = false;
    boolean afterQuote = false;
    int value;
    while ((value = reader.read()) != -1) {
      char character = (char) value;
      if (quoted) {
        if (character == '"') {
          int next = reader.read();
          if (next == '"') field.append('"');
          else {
            quoted = false;
            afterQuote = true;
            if (next != -1) reader.unread(next);
          }
        } else field.append(character);
      } else if (character == ',' || character == '\n' || character == '\r') {
        row.add(field.toString());
        field.setLength(0);
        afterQuote = false;
        if (character != ',') {
          if (row.size() > 1 || !row.get(0).isEmpty()) rows.add(row);
          row = new ArrayList<>();
          if (character == '\r') {
            int next = reader.read();
            if (next != '\n' && next != -1) reader.unread(next);
          }
        }
      } else if (character == '"' && field.isEmpty() && !afterQuote) quoted = true;
      else if (afterQuote || character == '"') throw new IllegalStateException("CSV de carga inicial inválido");
      else field.append(character);
    }
    if (quoted) throw new IllegalStateException("Campo sem aspas de fechamento no CSV de carga inicial");
    if (!row.isEmpty() || !field.isEmpty() || afterQuote) {
      row.add(field.toString());
      rows.add(row);
    }
    return rows;
  }
}
