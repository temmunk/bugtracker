package com.bugtracker.controller;

import com.bugtracker.dto.DashboardStats;
import com.bugtracker.exception.BugNotFoundException;
import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.security.CustomUserDetailsService;
import com.bugtracker.security.JwtUtil;
import com.bugtracker.service.BugService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

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

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

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
    @DisplayName("GET /api/bugs returns paginated bugs")
    void getAllBugs() throws Exception {
        Bug bug2 = new Bug("Page crash", "Dashboard crashes", BugPriority.CRITICAL, BugStatus.OPEN, "carol", null);
        bug2.setId(2L);
        Page<Bug> page = new PageImpl<>(Arrays.asList(sampleBug, bug2), PageRequest.of(0, 10), 2);
        when(bugService.getAllBugsPaged(any())).thenReturn(page);

        mockMvc.perform(get("/api/bugs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Login button broken")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("GET /api/bugs?status=OPEN filters by status")
    void getBugsByStatus() throws Exception {
        Page<Bug> page = new PageImpl<>(List.of(sampleBug), PageRequest.of(0, 10), 1);
        when(bugService.getBugsByStatusPaged(eq(BugStatus.OPEN), any())).thenReturn(page);

        mockMvc.perform(get("/api/bugs").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("OPEN")));
    }

    @Test
    @DisplayName("GET /api/bugs?search=login searches by keyword")
    void searchBugs() throws Exception {
        Page<Bug> page = new PageImpl<>(List.of(sampleBug), PageRequest.of(0, 10), 1);
        when(bugService.searchBugsPaged(eq("login"), any())).thenReturn(page);

        mockMvc.perform(get("/api/bugs").param("search", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/bugs returns empty page when no bugs exist")
    void getAllBugs_empty() throws Exception {
        Page<Bug> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(bugService.getAllBugsPaged(any())).thenReturn(page);

        mockMvc.perform(get("/api/bugs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
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
    @WithMockUser
    @DisplayName("POST /api/bugs creates a new bug")
    void createBug() throws Exception {
        when(bugService.createBug(any(Bug.class))).thenReturn(sampleBug);

        mockMvc.perform(post("/api/bugs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBug)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Login button broken")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/bugs returns 400 when title is blank")
    void createBug_validationError() throws Exception {
        Bug invalid = new Bug("", "desc", BugPriority.LOW, BugStatus.OPEN, null, null);

        mockMvc.perform(post("/api/bugs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
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
    @WithMockUser
    @DisplayName("DELETE /api/bugs/{id} returns 204 on success")
    void deleteBug() throws Exception {
        doNothing().when(bugService).deleteBug(1L);

        mockMvc.perform(delete("/api/bugs/1"))
                .andExpect(status().isNoContent());

        verify(bugService).deleteBug(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/bugs/{id} returns 404 when not found")
    void deleteBug_notFound() throws Exception {
        doThrow(new BugNotFoundException(99L)).when(bugService).deleteBug(99L);

        mockMvc.perform(delete("/api/bugs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/bugs/stats returns dashboard statistics")
    void getDashboardStats() throws Exception {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("OPEN", 5L);
        byStatus.put("CLOSED", 3L);
        Map<String, Long> byPriority = new LinkedHashMap<>();
        byPriority.put("HIGH", 4L);
        byPriority.put("LOW", 2L);
        DashboardStats stats = new DashboardStats(8, byStatus, byPriority, Map.of("bob", 3L));
        when(bugService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/bugs/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBugs", is(8)))
                .andExpect(jsonPath("$.byStatus.OPEN", is(5)));
    }

    @Test
    @DisplayName("GET /api/bugs/export returns CSV file")
    void exportCsv() throws Exception {
        when(bugService.getAllBugsForExport()).thenReturn(List.of(sampleBug));

        mockMvc.perform(get("/api/bugs/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("bugs_export.csv")))
                .andExpect(content().contentType("text/csv"));
    }
}
