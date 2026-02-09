package com.yourcompany.selfhealing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "element_interaction")
public class ElementInteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "navigation_type")
    private String navigationType;

    @Column(name = "before_uri", columnDefinition = "TINYTEXT")
    private String beforeUri;

    @Column(name = "after_uri", columnDefinition = "TINYTEXT")
    private String afterUri;

    @Column(name = "interaction_time", nullable = false)
    private LocalDateTime interactionTime;

    /* ✅ THIS FIELD IS REQUIRED */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "element_locator_id", nullable = false)
    private LocatorMetaEntity locator;

    @PrePersist
    public void onCreate() {
        interactionTime = LocalDateTime.now();
    }

    /* ================= GETTERS / SETTERS ================= */

    public LocatorMetaEntity getLocator() {
        return locator;
    }

    public void setLocator(LocatorMetaEntity locator) {
        this.locator = locator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getNavigationType() {
        return navigationType;
    }

    public void setNavigationType(String navigationType) {
        this.navigationType = navigationType;
    }

    public String getBeforeUri() {
        return beforeUri;
    }

    public void setBeforeUri(String beforeUri) {
        this.beforeUri = beforeUri;
    }

    public String getAfterUri() {
        return afterUri;
    }

    public void setAfterUri(String afterUri) {
        this.afterUri = afterUri;
    }

    public LocalDateTime getInteractionTime() {
        return interactionTime;
    }

    public void setInteractionTime(LocalDateTime interactionTime) {
        this.interactionTime = interactionTime;
    }
}
