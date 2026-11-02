package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.AdminDtos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Sprint2IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================================
    // TASK 1: PENDING PRACTICE REVIEWS
    // ========================================================

    @Test
    @DisplayName("Therapist can access pending practice reviews via /pending-reviews")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanAccessPendingReviews() throws Exception {
        mockMvc.perform(get("/api/caregiver-practice/pending-reviews"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Therapist can access pending practice reviews via /reviews/pending alias")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanAccessPendingReviewsAlias() throws Exception {
        mockMvc.perform(get("/api/caregiver-practice/reviews/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Caregiver cannot access therapist pending practice reviews (HTTP 403)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverCannotAccessPendingReviews() throws Exception {
        mockMvc.perform(get("/api/caregiver-practice/pending-reviews"))
                .andExpect(status().isForbidden());
    }

    // ========================================================
    // TASK 2: SUPER ADMIN CHILD REASSIGNMENT
    // ========================================================

    @Test
    @DisplayName("Therapist cannot reassign children (HTTP 403)")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCannotReassignChild() throws Exception {
        AdminDtos.ChildReassignRequest req = new AdminDtos.ChildReassignRequest();
        req.setNewTherapistId(1L);
        req.setReason("Caseload adjustment");

        mockMvc.perform(put("/api/admin/children/1/reassign-therapist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Caregiver cannot reassign children (HTTP 403)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverCannotReassignChild() throws Exception {
        AdminDtos.ChildReassignRequest req = new AdminDtos.ChildReassignRequest();
        req.setNewTherapistId(1L);
        req.setReason("Caseload adjustment");

        mockMvc.perform(put("/api/admin/children/1/reassign-therapist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Reassignment fails with 404 for non-existent child ID")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void reassignNonExistentChildReturns404() throws Exception {
        AdminDtos.ChildReassignRequest req = new AdminDtos.ChildReassignRequest();
        req.setNewTherapistId(1L);
        req.setReason("Caseload adjustment");

        mockMvc.perform(put("/api/admin/children/99999/reassign-therapist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Reassignment fails with 404 for non-existent therapist ID")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void reassignNonExistentTherapistReturns404() throws Exception {
        AdminDtos.ChildReassignRequest req = new AdminDtos.ChildReassignRequest();
        req.setNewTherapistId(99999L);
        req.setReason("Caseload adjustment");

        mockMvc.perform(put("/api/admin/children/1/reassign-therapist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ========================================================
    // TASK 3: GENUINE CLINICAL PDF REPORT GENERATION
    // ========================================================

    @Test
    @DisplayName("Super Admin can download clinical PDF report with %PDF- header")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void superAdminCanDownloadPdfReport() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/reports/children/1/pdf?activityId=1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("Selfora_Clinical_Report_Child_1.pdf")))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 500, "PDF content must be non-empty");

        // Verify authentic PDF magic bytes (%PDF-)
        String magicHeader = new String(content, 0, 5);
        assertEquals("%PDF-", magicHeader, "File must begin with valid binary %PDF- signature");
    }

    @Test
    @DisplayName("Assigned therapist can download clinical PDF report")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanDownloadPdfReport() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/reports/children/13/pdf?activityId=1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 500, "PDF content must be non-empty");
        assertEquals("%PDF-", new String(content, 0, 5));
    }

    @Test
    @DisplayName("Caregiver cannot download clinical evaluation PDF report (HTTP 403)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverCannotDownloadPdfReport() throws Exception {
        mockMvc.perform(get("/api/reports/children/1/pdf?activityId=1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PDF report fails with 404 for non-existent child ID")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void pdfReportNonExistentChildReturns404() throws Exception {
        mockMvc.perform(get("/api/reports/children/99999/pdf"))
                .andExpect(status().isNotFound());
    }

    // ========================================================
    // TASK 4: PASSWORD RESET CONTRACT CONSISTENCY
    // ========================================================

    @Test
    @DisplayName("Super Admin can reset caregiver password using JSON request body")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void adminCanResetCaregiverPasswordWithBody() throws Exception {
        AdminDtos.ResetPasswordRequestBody req = new AdminDtos.ResetPasswordRequestBody();
        req.setNewPassword("NewSecurePass123!");

        mockMvc.perform(post("/api/admin/caregivers/2/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Super Admin can reset therapist password using JSON request body")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void adminCanResetTherapistPasswordWithBody() throws Exception {
        AdminDtos.ResetPasswordRequestBody req = new AdminDtos.ResetPasswordRequestBody();
        req.setNewPassword("NewTherapistPass123!");

        mockMvc.perform(post("/api/admin/therapists/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}

