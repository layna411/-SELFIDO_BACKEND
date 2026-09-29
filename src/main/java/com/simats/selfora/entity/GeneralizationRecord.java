package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "generalization_records")
public class GeneralizationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "performance_record_id", nullable = false)
    private PerformanceRecord performanceRecord;

    @Column(nullable = false, length = 50)
    private String environment;

    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    public GeneralizationRecord() {}

    public GeneralizationRecord(Long id, PerformanceRecord performanceRecord, String environment, LocalDateTime recordedAt) {
        this.id = id;
        this.performanceRecord = performanceRecord;
        this.environment = environment;
        this.recordedAt = recordedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private PerformanceRecord performanceRecord;
        private String environment;
        private LocalDateTime recordedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder performanceRecord(PerformanceRecord performanceRecord) { this.performanceRecord = performanceRecord; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }
        public GeneralizationRecord build() { return new GeneralizationRecord(id, performanceRecord, environment, recordedAt); }
    }

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PerformanceRecord getPerformanceRecord() { return performanceRecord; }
    public void setPerformanceRecord(PerformanceRecord performanceRecord) { this.performanceRecord = performanceRecord; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
