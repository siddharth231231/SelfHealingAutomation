package com.yourcompany.selfhealing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "locator_metadata")
public class LocatorMetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= DOM STRUCTURE ================= */

    @Column(name = "ancestor_dom_path", columnDefinition = "TINYTEXT")
    private String ancestorDomPath;

    @Column(name = "ancestor_tags", columnDefinition = "TINYTEXT")
    private String ancestorTags;

    @Column(name = "clean_parent_dom", columnDefinition = "TINYTEXT")
    private String cleanParentDom;

    @Column(name = "parent_attributes", columnDefinition = "TINYTEXT")
    private String parentAttributes;

    @Column(name = "sibling_context", columnDefinition = "TINYTEXT")
    private String siblingContext;

    /* ================= PAGE INFO ================= */

    @Column(name = "page_name")
    private String pageName;

    @Column(name = "page_title", length = 500)
    private String pageTitle;

    @Column(name = "page_uri", columnDefinition = "TINYTEXT")
    private String pageUri;

    /* ================= LOCATOR INFO ================= */

    @Column(name = "locator_name", nullable = false)
    private String locatorName;

    @Column(name = "locator_type", nullable = false, length = 50)
    private String locatorType;

    @Column(name = "working_xpath", nullable = false, columnDefinition = "TINYTEXT")
    private String workingXpath;

    /* ================= HEALING SIGNALS ================= */

    @Column(name = "dom_hash", length = 128)
    private String domHash;

    @Column(name = "node_depth")
    private Integer nodeDepth;

    @Column(name = "has_text")
    private Boolean hasText;

    @Column(name = "text_content", columnDefinition = "TINYTEXT")
    private String textContent;

    /* ================= AUDIT ================= */

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* ================= JPA HOOKS ================= */

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /* ================= GETTERS / SETTERS ================= */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAncestorDomPath() {
        return ancestorDomPath;
    }

    public void setAncestorDomPath(String ancestorDomPath) {
        this.ancestorDomPath = ancestorDomPath;
    }

    public String getAncestorTags() {
        return ancestorTags;
    }

    public void setAncestorTags(String ancestorTags) {
        this.ancestorTags = ancestorTags;
    }

    public String getCleanParentDom() {
        return cleanParentDom;
    }

    public void setCleanParentDom(String cleanParentDom) {
        this.cleanParentDom = cleanParentDom;
    }

    public String getParentAttributes() {
        return parentAttributes;
    }

    public void setParentAttributes(String parentAttributes) {
        this.parentAttributes = parentAttributes;
    }

    public String getSiblingContext() {
        return siblingContext;
    }

    public void setSiblingContext(String siblingContext) {
        this.siblingContext = siblingContext;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getPageUri() {
        return pageUri;
    }

    public void setPageUri(String pageUri) {
        this.pageUri = pageUri;
    }

    public String getLocatorName() {
        return locatorName;
    }

    public void setLocatorName(String locatorName) {
        this.locatorName = locatorName;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public void setLocatorType(String locatorType) {
        this.locatorType = locatorType;
    }

    public String getWorkingXpath() {
        return workingXpath;
    }

    public void setWorkingXpath(String workingXpath) {
        this.workingXpath = workingXpath;
    }

    public String getDomHash() {
        return domHash;
    }

    public void setDomHash(String domHash) {
        this.domHash = domHash;
    }

    public Integer getNodeDepth() {
        return nodeDepth;
    }

    public void setNodeDepth(Integer nodeDepth) {
        this.nodeDepth = nodeDepth;
    }

    public Boolean getHasText() {
        return hasText;
    }

    public void setHasText(Boolean hasText) {
        this.hasText = hasText;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
