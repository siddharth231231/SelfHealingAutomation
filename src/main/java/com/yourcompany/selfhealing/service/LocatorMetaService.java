package com.yourcompany.selfhealing.service;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.repository.LocatorMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocatorMetaService {

    private final LocatorMetaRepository locatorRepo;

    public LocatorMetaService(LocatorMetaRepository locatorRepo) {
        this.locatorRepo = locatorRepo;
    }

    /* ================= CREATE ================= */

    public LocatorMetaEntity saveIfNotExists(LocatorMetaEntity locator) {

        Optional<LocatorMetaEntity> existing =
                locatorRepo.findByPageUrlAndLocatorName(
                        locator.getPageUrl(),
                        locator.getLocatorName());

        return existing.orElseGet(() -> locatorRepo.save(locator));
    }

    /* ================= LOOKUPS ================= */

    public Optional<LocatorMetaEntity> findByLocatorName(String locatorName) {
        return locatorRepo.findByLocatorName(locatorName);
    }

    public Optional<LocatorMetaEntity> findByPageUrlAndName(
            String pageUrl,
            String locatorName) {

        return locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName);
    }

    public Optional<LocatorMetaEntity> findByDomHash(String domHash) {
        return locatorRepo.findByDomHash(domHash);
    }

    public List<LocatorMetaEntity> findAllByPageUrl(String pageUrl) {
        return locatorRepo.findByPageUrl(pageUrl);
    }

    public Optional<LocatorMetaEntity> findById(Long id) {
        return locatorRepo.findById(id);
    }

    /* ================= SELF-HEALING UPDATE ================= */

    /**
     * Called when healing succeeds.
     */
    public void updateAfterHealing(
            String pageUrl,
            String locatorName,
            String newXpath,
            String newDomSnapshot,
            String newDomHash,
            Double similarityScore) {

        LocatorMetaEntity locator =
                locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName)
                        .orElseThrow(() ->
                                new RuntimeException("Locator not found: " + locatorName));

        // Update healed locator
        locator.setCurrentActiveLocator(newXpath);

        // Update snapshot + hash
        locator.setDomSnapshot(newDomSnapshot);
        locator.setDomHash(newDomHash);

        // Increment version
        locator.setLocatorVersion(locator.getLocatorVersion() + 1);

        // Increment heal count
        locator.setHealCount(locator.getHealCount() + 1);

        // Save similarity score
        locator.setLastSimilarityScore(similarityScore);

        locatorRepo.save(locator);
    }

    /* ================= MANUAL UPDATE ================= */

    public void updateActiveLocator(String pageUrl, String locatorName, String newXpath) {

        LocatorMetaEntity locator =
                locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName)
                        .orElseThrow(() ->
                                new RuntimeException("Locator not found: " + locatorName));

        locator.setCurrentActiveLocator(newXpath);

        locatorRepo.save(locator);
    }

    /* ================= ANALYTICS SUPPORT ================= */

    public List<LocatorMetaEntity> findFrequentlyHealed(Integer minHealCount) {
        return locatorRepo.findByHealCountGreaterThan(minHealCount);
    }
}