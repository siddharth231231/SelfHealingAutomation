package com.yourcompany.selfhealing.repository;

import com.yourcompany.selfhealing.entity.ElementInteractionEntity;
import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ElementInteractionRepository
        extends JpaRepository<ElementInteractionEntity, Long> {

    /* ================= BASIC RELATION ================= */

    List<ElementInteractionEntity> findByLocator(
            LocatorMetaEntity locator);

    /* ================= ACTION ANALYSIS ================= */

    List<ElementInteractionEntity> findByActionType(String actionType);

    List<ElementInteractionEntity> findByNavigationType(String navigationType);

    /* ================= TIME-BASED ================= */

    List<ElementInteractionEntity> findByInteractionTimeBetween(
            LocalDateTime start,
            LocalDateTime end);

    /* ================= NAVIGATION CHANGE ================= */

    List<ElementInteractionEntity> findByBeforeUriAndAfterUri(
            String beforeUri,
            String afterUri);
}
