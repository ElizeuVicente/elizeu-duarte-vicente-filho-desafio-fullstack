package com.oxyn.notes_api.note.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.oxyn.notes_api.shared.exception.ApiExceptionHandler;
import com.oxyn.notes_api.shared.exception.ResourceNotFoundException;
import com.oxyn.notes_api.note.service.NoteService;

class NoteControllerErrorTests {
  private MockMvc mvc;
  private NoteService service;

  @BeforeEach
  void setUp() {
    service = mock(NoteService.class);
    mvc = MockMvcBuilders.standaloneSetup(new NoteController(service))
        .setControllerAdvice(new ApiExceptionHandler()).build();
  }

  @Test
  void createWithInvalidFieldsReturnsFieldErrors() throws Exception {
    mvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
            .content("{\"site\":\"\",\"equipment\":\"\",\"variable\":\"\",\"author\":\"\",\"message\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Dados inválidos"))
        .andExpect(jsonPath("$.path").value("/notes"))
        .andExpect(jsonPath("$.fieldErrors[?(@.field == 'timestamp')]").exists())
        .andExpect(jsonPath("$.fieldErrors[?(@.field == 'site')]").exists());
  }

  @Test
  void updateWithInvalidFieldsReturnsBadRequest() throws Exception {
    mvc.perform(put("/notes/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"site\":\"\",\"equipment\":\"Gerador\",\"variable\":\"Temperatura\",\"timestamp\":null,\"author\":\"Maria\",\"message\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[?(@.field == 'timestamp')]").exists());
  }

  @Test
  void malformedJsonReturnsBadRequest() throws Exception {
    mvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("JSON inválido ou campo com formato incorreto"));
  }

  @Test
  void unknownNoteReturnsNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.get(id)).thenThrow(new ResourceNotFoundException("Nota não encontrada: " + id));
    mvc.perform(get("/notes/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Nota não encontrada: " + id));
  }

  @Test
  void invalidQueryReturnsBadRequest() throws Exception {
    mvc.perform(get("/notes?size=101"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    mvc.perform(get("/notes?startDate=2024-02-01&endDate=2024-01-01"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/notes?startDate=invalid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Parâmetro inválido: startDate"));
  }

  @Test
  void invalidUuidReturnsBadRequest() throws Exception {
    mvc.perform(get("/notes/not-a-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Parâmetro inválido: id"));
  }

  @Test
  void unexpectedFailureReturnsSafeMessage() throws Exception {
    when(service.get(org.mockito.ArgumentMatchers.any(UUID.class)))
        .thenThrow(new IllegalStateException("internal database detail"));
    mvc.perform(get("/notes/" + UUID.randomUUID()))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Erro interno do servidor"));
  }

  @Test
  void unsupportedHttpMethodKeepsItsStatus() throws Exception {
    mvc.perform(patch("/notes"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.message").value("Método HTTP não permitido"));
  }
}
