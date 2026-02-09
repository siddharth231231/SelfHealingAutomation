package com.yourcompany.selfhealing.repository;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocatorMetaRepository
        extends JpaRepository<LocatorMetaEntity, Long> {

    /* ================= CORE LOOKUPS ================= */

    Optional<LocatorMetaEntity> findByPageNameAndLocatorName(
            String pageName,
            String locatorName);

    Optional<LocatorMetaEntity> findByDomHash(String domHash);

    /* ================= HEALING SUPPORT ================= */

    List<LocatorMetaEntity> findByPageName(String pageName);

    List<LocatorMetaEntity> findByLocatorType(String locatorType);

    List<LocatorMetaEntity> findByNodeDepthLessThan(Integer depth);

    List<LocatorMetaEntity> findByHasText(Boolean hasText);
}
