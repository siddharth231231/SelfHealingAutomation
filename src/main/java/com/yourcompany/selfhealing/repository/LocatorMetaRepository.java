package com.yourcompany.selfhealing.repository;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocatorMetaRepository
        extends JpaRepository<LocatorMetaEntity, Long> {

    /* ================= CORE LOOKUPS ================= */

    // Primary lookup during healing
    Optional<LocatorMetaEntity> findByLocatorName(String locatorName);

    // If locator name can exist on multiple pages
    Optional<LocatorMetaEntity> findByPageUrlAndLocatorName(
            String pageUrl,
            String locatorName
    );

    // Get all locators from a specific page
    List<LocatorMetaEntity> findByPageUrl(String pageUrl);

    /* ================= XPATH SUPPORT ================= */

    Optional<LocatorMetaEntity> findByRelativeXpath(String relativeXpath);

    Optional<LocatorMetaEntity> findByAbsoluteXpath(String absoluteXpath);

    /* ================= DOM HASH SUPPORT ================= */

    Optional<LocatorMetaEntity> findByDomHash(String domHash);

    Optional<LocatorMetaEntity> findByPageUrlAndDomHash(
            String pageUrl,
            String domHash
    );

    /* ================= VERSIONING SUPPORT ================= */

    // Get latest version of a locator
    Optional<LocatorMetaEntity> findTopByLocatorNameOrderByLocatorVersionDesc(
            String locatorName
    );

    /* ================= HEAL ANALYTICS ================= */

    // Get elements healed more than X times (useful for debugging)
    List<LocatorMetaEntity> findByHealCountGreaterThan(Integer count);
}