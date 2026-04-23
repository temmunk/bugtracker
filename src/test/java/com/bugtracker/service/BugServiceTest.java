package com.bugtracker.service;

import com.bugtracker.exception.BugNotFoundException;
import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.repository.BugRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BugServiceTest {

    @Mock
    private BugRepository bugRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private BugService bugService;

    private Bug sampleBug;

    @BeforeEach
    void setUp() {
        sampleBug = new Bug("Login button broken", "Cannot click login on mobile",
                BugPriority.HIGH, BugStatus.OPEN, "alice", "bob");
        sampleBug.setId(1L);
    }

    @Test
    @DisplayName("getAllBugs returns all bugs from repository")
    void getAllBugs_returnsList() {
        Bug bug2 = new Bug("Page crash", "Dashboard crashes", BugPriority.CRITICAL, BugStatus.OPEN, "carol", null);
        when(bugRepository.findAll()).thenReturn(Arrays.asList(sampleBug, bug2));

        List<Bug> result = bugService.getAllBugs();

        assertEquals(2, result.size());
        verify(bugRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getBugById returns bug when found")
    void getBugById_found() {
        when(bugRepository.findById(1L)).thenReturn(Optional.of(sampleBug));

        Bug result = bugService.getBugById(1L);

        assertNotNull(result);
        assertEquals("Login button broken", result.getTitle());
    }

    @Test
    @DisplayName("getBugById throws BugNotFoundException when not found")
    void getBugById_notFound() {
        when(bugRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BugNotFoundException.class, () -> bugService.getBugById(99L));
    }

    @Test
    @DisplayName("createBug sets default OPEN status when status is null")
    void createBug_defaultStatus() {
        Bug newBug = new Bug("New issue", "desc", BugPriority.LOW, null, "reporter", null);
        when(bugRepository.save(any(Bug.class))).thenReturn(newBug);

        bugService.createBug(newBug);

        assertEquals(BugStatus.OPEN, newBug.getStatus());
        verify(bugRepository).save(newBug);
    }

    @Test
    @DisplayName("createBug preserves explicit status")
    void createBug_preservesStatus() {
        Bug newBug = new Bug("Bug", "desc", BugPriority.MEDIUM, BugStatus.IN_PROGRESS, "reporter", null);
        when(bugRepository.save(any(Bug.class))).thenReturn(newBug);

        bugService.createBug(newBug);

        assertEquals(BugStatus.IN_PROGRESS, newBug.getStatus());
    }

    @Test
    @DisplayName("updateBug modifies existing bug fields")
    void updateBug_success() {
        Bug updated = new Bug("Updated title", "Updated desc", BugPriority.LOW, BugStatus.RESOLVED, "alice", "dave");
        when(bugRepository.findById(1L)).thenReturn(Optional.of(sampleBug));
        when(bugRepository.save(any(Bug.class))).thenReturn(sampleBug);

        Bug result = bugService.updateBug(1L, updated);

        assertEquals("Updated title", result.getTitle());
        assertEquals(BugPriority.LOW, result.getPriority());
        assertEquals(BugStatus.RESOLVED, result.getStatus());
        assertEquals("dave", result.getAssignee());
    }

    @Test
    @DisplayName("updateBug throws when bug does not exist")
    void updateBug_notFound() {
        when(bugRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BugNotFoundException.class,
                () -> bugService.updateBug(99L, sampleBug));
    }

    @Test
    @DisplayName("deleteBug removes existing bug")
    void deleteBug_success() {
        when(bugRepository.findById(1L)).thenReturn(Optional.of(sampleBug));
        doNothing().when(bugRepository).delete(sampleBug);

        bugService.deleteBug(1L);

        verify(bugRepository).delete(sampleBug);
    }

    @Test
    @DisplayName("deleteBug throws when bug does not exist")
    void deleteBug_notFound() {
        when(bugRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BugNotFoundException.class, () -> bugService.deleteBug(99L));
    }

    @Test
    @DisplayName("getBugsByStatus filters correctly")
    void getBugsByStatus() {
        when(bugRepository.findByStatus(BugStatus.OPEN)).thenReturn(List.of(sampleBug));

        List<Bug> result = bugService.getBugsByStatus(BugStatus.OPEN);

        assertEquals(1, result.size());
        assertEquals(BugStatus.OPEN, result.get(0).getStatus());
    }

    @Test
    @DisplayName("searchBugs delegates to repository")
    void searchBugs() {
        when(bugRepository.findByTitleContainingIgnoreCase("login")).thenReturn(List.of(sampleBug));

        List<Bug> result = bugService.searchBugs("login");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().toLowerCase().contains("login"));
    }
}
