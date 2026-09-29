package com.simats.selfora.dto;

import java.time.LocalDate;

public class ChildDto {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String avatarUrl;
    private String diagnosisNotes;
    private Boolean isActive;
    private String caregiverName;
    private String therapistName;

    public ChildDto() {}

    public ChildDto(Long id, String firstName, String lastName, LocalDate dateOfBirth, String gender, String avatarUrl, String diagnosisNotes, Boolean isActive, String caregiverName, String therapistName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
        this.diagnosisNotes = diagnosisNotes;
        this.isActive = isActive;
        this.caregiverName = caregiverName;
        this.therapistName = therapistName;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String gender;
        private String avatarUrl;
        private String diagnosisNotes;
        private Boolean isActive;
        private String caregiverName;
        private String therapistName;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder dateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder diagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder caregiverName(String caregiverName) { this.caregiverName = caregiverName; return this; }
        public Builder therapistName(String therapistName) { this.therapistName = therapistName; return this; }
        public ChildDto build() { return new ChildDto(id, firstName, lastName, dateOfBirth, gender, avatarUrl, diagnosisNotes, isActive, caregiverName, therapistName); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getDiagnosisNotes() { return diagnosisNotes; }
    public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCaregiverName() { return caregiverName; }
    public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }

    public String getTherapistName() { return therapistName; }
    public void setTherapistName(String therapistName) { this.therapistName = therapistName; }
}
