package com.simats.selfora.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "micro_skills")
public class MicroSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private TaskStep step;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(columnDefinition = "TEXT")
    private String description;

    public MicroSkill() {}

    public MicroSkill(Long id, TaskStep step, String name, Integer sequenceOrder, String description) {
        this.id = id;
        this.step = step;
        this.name = name;
        this.sequenceOrder = sequenceOrder;
        this.description = description;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private TaskStep step;
        private String name;
        private Integer sequenceOrder;
        private String description;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder step(TaskStep step) { this.step = step; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder sequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public MicroSkill build() { return new MicroSkill(id, step, name, sequenceOrder, description); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TaskStep getStep() { return step; }
    public void setStep(TaskStep step) { this.step = step; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
