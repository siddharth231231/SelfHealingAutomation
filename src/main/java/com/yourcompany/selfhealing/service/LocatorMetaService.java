package com.yourcompany.selfhealing.service;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.repository.LocatorMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LocatorMetaService {

    private final LocatorMetaRepository locatorRepo;

    public LocatorMetaService(LocatorMetaRepository locatorRepo) {
        this.locatorRepo = locatorRepo;
    }

    /* ================= CREATE / UPDATE ================= */

    public LocatorMetaEntity save(LocatorMetaEntity locator) {
        return locatorRepo.save(locator);
    }

    /* ================= LOOKUPS ================= */

    public Optional<LocatorMetaEntity> findByPageAndName(
            String pageName,
            String locatorName) {

        return locatorRepo.findByPageNameAndLocatorName(pageName, locatorName);
    }

    public Optional<LocatorMetaEntity> findById(Long id) {
        return locatorRepo.findById(id);
    }

    /* ================= SELF-HEALING HOOKS ================= */

    public void updateWorkingXpath(Long locatorId, String newXpath) {
        LocatorMetaEntity locator = locatorRepo.findById(locatorId)
                .orElseThrow(() -> new RuntimeException("Locator not found"));

        locator.setWorkingXpath(newXpath);
        locatorRepo.save(locator);
    }
}
