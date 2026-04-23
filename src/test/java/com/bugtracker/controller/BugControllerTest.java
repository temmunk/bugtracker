package com.bugtracker.controller;

import com.bugtracker.exception.BugNotFoundException;
import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.service.BugService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BugController.class)
class BugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BugService bugService;

    @Autowired
    private ObjectMapper objectMapper;

    private Bug sampleBug;

    @BeforeEach
    void setUp() {
        sampleBug = new Bug("Login button broken", "Cannot click login on mobile",
                BugPriority.HIGH, BugStatus.OPEN, "alice", "bob");
        sampleBug.setId(1L);
    }

    @Test
    @DisplayName("GET /api/bugs returns all bugs")
    void getAllBugs() throws Exception {
        Bug bug2 = new Bug("Page crash", "Dashboard crashes", BugPriority.CRITICAL, BugStatus.OPEN, "carol", null);
        bug2.setId(2L);
        when(bugService.getAllBugs()).thenReturn(Arrays.asList(sampleBug, bug2));

        mockMvc.perform(get("/api/bugs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Login button broken")))
                .andExpect(jsonPath("$[1].title", is("Page crash")));
    }

    @Test
    @DisplayName("GET /api/bugs?status=OPEN filters by status")
    void getBugsByStatus() throws Exception {
        when(bugService.getBugsByStatus(BugStatus.OPEN)).thenReturn(List.of(sampleBug));

        mockMvc.perform(get("/api/bugs").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("OPEN")));
    }

    @Test
    @DisplayName("GET /api/bugs?search=login searches by keyword")
    void searchBugs() throws Exception {
        when(bugService.searchBugs("login")).thenReturn(List.of(sampleBug));

        mockMvc.perform(get("/api/bugs").param("search", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/bugs returns empty list when no bugs exist")
    void getAllBugs_empty() throws Exception {
        when(bugService.getAllBugs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bugs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/bugs/{id} returns bug when found")
    void getBugById_found() throws Exception {
        when(bugService.getBugById(1L)).thenReturn(sampleBug);

        mockMvc.perform(get("/api/bugs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Login button broken")))
                .andExpect(jsonPath("$.priority", is("HIGH")));
    }

    @Test
    @DisplayName("GET /api/bugs/{id} returns 404 when not found")
    void getBugById_notFound() throws Exception {
        when(bugService.getBugById(99L)).thenThrow(new BugNotFoundException(99L));

        mockMvc.perform(get("/api/bugs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("99")));
    }

    @Test
    @DisplayName("POST /api/bugs creates a new bug")
    void createBug() throws Exception {
        when(bugService.createBug(any(Bug.class))).thenReturn(sampleBug);

        String json = objectMapper.writeValueAsString(sampleBug);

        mockMvc.perform(post("/api/bugs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Login button broken")));
    }

    @Test
    @DisplayName("POST /api/bugs returns 400 when title is blank")
    void createBug_validationError() throws Exception {
        Bug invalid = new Bug("", "desc", BugPriority.LOW, BugStatus.OPEN, null, null);

        mockMvc.perform(post("/api/bugs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/bugs/{id} updates existing bug")
    void updateBug() throws Exception {
        Bug updated = new Bug("Updated title", "Updated desc", BugPriority.LOW, BugStatus.RESOLVED, "alice", "dave");
        updated.setId(1L);
        when(bugService.updateBug(eq(1L), any(Bug.class))).thenReturn(updated);

        mockMvc.perform(put("/api/bugs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated title")))
                .andExpect(jsonPath("$.status", is("RESOLVED")));
    }

    @Test
    @DisplayName("DELETE /api/bugs/{id} returns 204 on success")
    void deleteBug() throws Exception {
        doNothing().when(bugService).deleteBug(1L);

        mockMvc.perform(delete("/api/bugs/1"))
                .andExpect(status().isNoContent());

        verify(bugService).deleteBug(1L);
    }

    @Test
    @DisplayName("DELETE /api/bugs/{id} returns 404 when not found")
    void deleteBug_notFound() throws Exception {
        doThrow(new BugNotFoundException(99L)).when(bugService).deleteBug(99L);

        mockMvc.perform(delete("/api/bugs/99"))
                .andExpect(status().isNotFound());
    }
}
