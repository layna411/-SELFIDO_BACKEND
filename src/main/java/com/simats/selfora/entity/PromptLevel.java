package com.simats.selfora.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prompt_levels")
public class PromptLevel {
    @Id
    private Integer id;

    @Column(name = "level_code", nullable = false, unique = true, length = 50)
    private String levelCode;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "hierarchy_order", nullable = false)
    private Integer hierarchyOrder;

    public PromptLevel() {}

    public PromptLevel(Integer id, String levelCode, String levelName, String description, Integer hierarchyOrder) {
        this.id = id;
        this.levelCode = levelCode;
        this.levelName = levelName;
        this.description = description;
        this.hierarchyOrder = hierarchyOrder;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private String levelCode;
        private String levelName;
        private String description;
        private Integer hierarchyOrder;

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder levelCode(String levelCode) { this.levelCode = levelCode; return this; }
        public Builder levelName(String levelName) { this.levelName = levelName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder hierarchyOrder(Integer hierarchyOrder) { this.hierarchyOrder = hierarchyOrder; return this; }
        public PromptLevel build() { return new PromptLevel(id, levelCode, levelName, description, hierarchyOrder); }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getHierarchyOrder() { return hierarchyOrder; }
    public void setHierarchyOrder(Integer hierarchyOrder) { this.hierarchyOrder = hierarchyOrder; }
}
