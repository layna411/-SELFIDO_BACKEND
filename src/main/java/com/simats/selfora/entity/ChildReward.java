package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "child_rewards", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cr_child_badge", columnNames = {"child_id", "badge_type"})
})
public class ChildReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @Column(name = "badge_type", nullable = false, length = 50)
    private String badgeType;

    @Column(name = "badge_title", nullable = false, length = 100)
    private String badgeTitle;

    @Column(name = "badge_icon", length = 100)
    private String badgeIcon;

    @Column(name = "earned_at", updatable = false)
    private LocalDateTime earnedAt;

    public ChildReward() {}

    public ChildReward(Child child, String badgeType, String badgeTitle, String badgeIcon) {
        this.child = child;
        this.badgeType = badgeType;
        this.badgeTitle = badgeTitle;
        this.badgeIcon = badgeIcon;
    }

    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public String getBadgeTitle() { return badgeTitle; }
    public void setBadgeTitle(String badgeTitle) { this.badgeTitle = badgeTitle; }

    public String getBadgeIcon() { return badgeIcon; }
    public void setBadgeIcon(String badgeIcon) { this.badgeIcon = badgeIcon; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
}
