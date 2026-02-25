package com.yourcompany.selfhealing.service;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.repository.LocatorMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocatorMetaService {

    private final LocatorMetaRepository locatorRepo;

    public LocatorMetaService(LocatorMetaRepository locatorRepo) {
        this.locatorRepo = locatorRepo;
    }

    /* =======================================================
       CREATE
       ======================================================= */

    public LocatorMetaEntity saveIfNotExists(LocatorMetaEntity locator) {

        return locatorRepo
                .findByPageUrlAndLocatorName(
                        locator.getPageUrl(),
                        locator.getLocatorName()
                )
                .orElseGet(() -> locatorRepo.save(locator));
    }

    /* =======================================================
       LOOKUPS
       ======================================================= */

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

    /* =======================================================
       SELF-HEALING UPDATE
       ======================================================= */

    /**
     * Called only when healing is successful.
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
                                new IllegalStateException(
                                        "Locator not found: " + locatorName
                                ));

        // Avoid unnecessary update
        if (newXpath != null && !newXpath.equals(locator.getCurrentActiveLocator())) {
            locator.setCurrentActiveLocator(newXpath);
        }

        if (newDomSnapshot != null) {
            locator.setDomSnapshot(newDomSnapshot);
        }

        if (newDomHash != null) {
            locator.setDomHash(newDomHash);
        }

        // Safe increment
        locator.setLocatorVersion(
                locator.getLocatorVersion() == null
                        ? 1
                        : locator.getLocatorVersion() + 1
        );

        locator.setHealCount(
                locator.getHealCount() == null
                        ? 1
                        : locator.getHealCount() + 1
        );

        locator.setLastSimilarityScore(similarityScore);
        locator.setLastHealedAt(LocalDateTime.now());
        locator.setLastValidatedAt(LocalDateTime.now());
        locator.setHealSuccessCount(
                locator.getHealSuccessCount() == null
                        ? 1
                        : locator.getHealSuccessCount() + 1
        );

        if (similarityScore != null) {
            Double previousAverage = locator.getAverageValidationScore();
            Integer previousCount = locator.getHealSuccessCount() != null
                    ? Math.max(locator.getHealSuccessCount() - 1, 0)
                    : 0;
            if (previousAverage == null || previousCount == 0) {
                locator.setAverageValidationScore(similarityScore);
            } else {
                double nextAverage = ((previousAverage * previousCount) + similarityScore) / (previousCount + 1);
                locator.setAverageValidationScore(nextAverage);
            }
        }

        locatorRepo.save(locator);
    }

    /* =======================================================
       SIMPLE ACTIVE LOCATOR UPDATE
       ======================================================= */

    public void updateActiveLocator(
            String pageUrl,
            String locatorName,
            String newXpath) {

        LocatorMetaEntity locator =
                locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Locator not found: " + locatorName
                                ));

        locator.setCurrentActiveLocator(newXpath);

        locatorRepo.save(locator);
    }

    /* =======================================================
       ANALYTICS
       ======================================================= */

    public List<LocatorMetaEntity> findFrequentlyHealed(Integer minHealCount) {
        return locatorRepo.findByHealCountGreaterThan(minHealCount);
    }

    public boolean exists(String pageUrl, String locatorName) {
        return locatorRepo
                .findByPageUrlAndLocatorName(pageUrl, locatorName)
                .isPresent();
    }
}
