package com.yourcompany.selfhealing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.selfhealing.dto.HealingHistoryRecordDto;
import com.yourcompany.selfhealing.dto.LocatorDetailDto;
import com.yourcompany.selfhealing.dto.LocatorSummaryDto;
import com.yourcompany.selfhealing.entity.HealingHistoryEntity;
import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.repository.HealingHistoryRepository;
import com.yourcompany.selfhealing.repository.LocatorMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

@Service
@Transactional
public class LocatorMetaService {

    private final LocatorMetaRepository locatorRepo;
    private final HealingHistoryRepository healingHistoryRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocatorMetaService(
            LocatorMetaRepository locatorRepo,
            HealingHistoryRepository healingHistoryRepo) {
        this.locatorRepo = locatorRepo;
        this.healingHistoryRepo = healingHistoryRepo;
    }

    public LocatorMetaEntity saveIfNotExists(LocatorMetaEntity locator) {
        return locatorRepo
                .findByPageUrlAndLocatorName(locator.getPageUrl(), locator.getLocatorName())
                .orElseGet(() -> locatorRepo.save(locator));
    }


    public LocatorMetaEntity upsertIfDomChanged(LocatorMetaEntity incoming) {
        Optional<LocatorMetaEntity> existingOpt =
                locatorRepo.findByPageUrlAndLocatorName(incoming.getPageUrl(), incoming.getLocatorName());

        if (existingOpt.isEmpty()) {
            incoming.setCurrentActiveLocator(incoming.getOriginalLocator());
            return locatorRepo.save(incoming);
        }

        LocatorMetaEntity existing = existingOpt.get();
        if (isSameHash(existing.getDomHash(), incoming.getDomHash())) {
            return existing;
        }

        existing.setOriginalLocator(incoming.getOriginalLocator());
        existing.setCurrentActiveLocator(incoming.getOriginalLocator());
        existing.setRelativeXpath(incoming.getRelativeXpath());
        existing.setAbsoluteXpath(incoming.getAbsoluteXpath());
        existing.setCssSelector(incoming.getCssSelector());
        existing.setParentXpath(incoming.getParentXpath());
        existing.setSiblingXpaths(incoming.getSiblingXpaths());
        existing.setParentXpathChain(incoming.getParentXpathChain());
        existing.setSiblingXpathCluster(incoming.getSiblingXpathCluster());
        existing.setElementTag(incoming.getElementTag());
        existing.setElementRole(incoming.getElementRole());
        existing.setElementType(incoming.getElementType());
        existing.setNormalizedVisibleText(incoming.getNormalizedVisibleText());
        existing.setStableAttributeJson(incoming.getStableAttributeJson());
        existing.setVolatileAttributeJson(incoming.getVolatileAttributeJson());
        existing.setAnchorHierarchyJson(incoming.getAnchorHierarchyJson());
        existing.setSiblingSignatureJson(incoming.getSiblingSignatureJson());
        existing.setElementFingerprintJson(incoming.getElementFingerprintJson());
        existing.setStructuralFingerprintJson(incoming.getStructuralFingerprintJson());
        existing.setNodePathJson(incoming.getNodePathJson());
        existing.setSemanticPath(incoming.getSemanticPath());
        existing.setDataTestId(incoming.getDataTestId());
        existing.setParentTag(incoming.getParentTag());
        existing.setParentId(incoming.getParentId());
        existing.setParentClass(incoming.getParentClass());
        existing.setPageTitle(incoming.getPageTitle());
        existing.setDomHash(incoming.getDomHash());
        existing.setDomSnapshot(incoming.getDomSnapshot());

        existing.setLocatorVersion(
                existing.getLocatorVersion() == null ? 1 : existing.getLocatorVersion() + 1
        );
        return locatorRepo.save(existing);
    }

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

    public List<LocatorSummaryDto> getLocatorSummaries() {
        return locatorRepo.findAll().stream()
            .map(this::toSummary)
            .sorted(Comparator.comparingInt((LocatorSummaryDto dto) -> safeHealCount(dto.healCount())).reversed())
                .toList();
    }

    public List<LocatorSummaryDto> getFrequentlyHealed(int minHealCount) {
        int floor = Math.max(minHealCount, 0);
        return locatorRepo.findByHealCountGreaterThan(floor).stream()
            .map(this::toSummary)
            .sorted(Comparator.comparingInt((LocatorSummaryDto dto) -> safeHealCount(dto.healCount())).reversed())
                .toList();
    }

    public Optional<LocatorDetailDto> getLocatorDetail(String locatorName) {
        if (locatorName == null || locatorName.isBlank()) {
            return Optional.empty();
        }
        return locatorRepo.findTopByLocatorNameOrderByLocatorVersionDesc(locatorName)
                .map(entity -> toDetail(entity, fetchHistory(locatorName, 20)));
    }

    public List<HealingHistoryRecordDto> getHealingHistory(String locatorName, int limit) {
        if (locatorName == null || locatorName.isBlank()) {
            return List.of();
        }
        return fetchHistory(locatorName, sanitizeLimit(limit));
    }

    public void updateAfterHealing(
            String pageUrl,
            String locatorName,
            String newXpath,
            String newStructuralFingerprintJson,
            String newDomHash,
            Double similarityScore) {

        LocatorMetaEntity locator =
                locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName)
                        .orElseThrow(() -> new IllegalStateException("Locator not found: " + locatorName));

        if (newXpath != null && !newXpath.isBlank()) {
            locator.setCurrentActiveLocator(newXpath);
        }

        if (newStructuralFingerprintJson != null && !newStructuralFingerprintJson.isBlank()) {
            locator.setStructuralFingerprintJson(newStructuralFingerprintJson);
        }

        if (newDomHash != null && !newDomHash.isBlank()) {
            locator.setDomHash(newDomHash);
        }

        locator.setHealCount(locator.getHealCount() == null ? 1 : locator.getHealCount() + 1);
        locator.setHealSuccessCount(locator.getHealSuccessCount() == null ? 1 : locator.getHealSuccessCount() + 1);
        locator.setLastSimilarityScore(similarityScore);
        locator.setLastHealedAt(LocalDateTime.now());
        locator.setLastValidatedAt(LocalDateTime.now());

        if (similarityScore != null) {
            Double oldAverage = locator.getAverageValidationScore();
            int successes = Math.max((locator.getHealSuccessCount() == null ? 1 : locator.getHealSuccessCount()) - 1, 0);
            if (oldAverage == null || successes == 0) {
                locator.setAverageValidationScore(similarityScore);
            } else {
                locator.setAverageValidationScore(((oldAverage * successes) + similarityScore) / (successes + 1));
            }
        }

        locatorRepo.save(locator);
    }

    public void incrementHealingFailure(String pageUrl, String locatorName) {
        locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName).ifPresent(locator -> {
            locator.setHealCount(locator.getHealCount() == null ? 1 : locator.getHealCount() + 1);
            locator.setHealFailureCount(locator.getHealFailureCount() == null ? 1 : locator.getHealFailureCount() + 1);
            locator.setLastValidatedAt(LocalDateTime.now());
            locatorRepo.save(locator);
        });
    }

    public void recordHealingHistory(
            String locatorName,
            String pageUrl,
            int attempt,
            String status,
            String failureReason,
            List<String> aiSuggestions,
            String selectedXpath,
            String healeniumXpath,
            Double healeniumScore) {

        HealingHistoryEntity history = new HealingHistoryEntity();
        history.setLocatorName(locatorName);
        history.setPageUrl(pageUrl);
        history.setAttemptNumber(attempt);
        history.setStatus(status);
        history.setFailureReason(failureReason);
        history.setAiSuggestionsJson(toJson(aiSuggestions));
        history.setSelectedXpath(selectedXpath);
        history.setHealeniumXpath(healeniumXpath);
        history.setHealeniumScore(healeniumScore);
        healingHistoryRepo.save(history);
    }

    public void updateActiveLocator(
            String pageUrl,
            String locatorName,
            String newXpath) {
        locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName).ifPresent(locator -> {
            locator.setCurrentActiveLocator(newXpath);
            locatorRepo.save(locator);
        });
    }

    public List<LocatorMetaEntity> findFrequentlyHealed(Integer minHealCount) {
        return locatorRepo.findByHealCountGreaterThan(minHealCount);
    }

    private List<HealingHistoryRecordDto> fetchHistory(String locatorName, int limit) {
        return healingHistoryRepo.findByLocatorNameOrderByCreatedAtDesc(locatorName).stream()
                .limit(limit)
                .map(this::toHistoryRecord)
                .toList();
    }

    private int sanitizeLimit(int limit) {
        int candidate = Math.max(limit, 1);
        return Math.min(candidate, 200);
    }

    private LocatorSummaryDto toSummary(LocatorMetaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new LocatorSummaryDto(
                entity.getLocatorName(),
                entity.getPageUrl(),
                entity.getHealCount(),
                entity.getHealSuccessCount(),
                entity.getHealFailureCount(),
                entity.getAverageValidationScore(),
                entity.getLastHealedAt(),
                entity.getLastValidatedAt()
        );
    }

    private LocatorDetailDto toDetail(LocatorMetaEntity entity, List<HealingHistoryRecordDto> history) {
        return new LocatorDetailDto(
                entity.getLocatorName(),
                entity.getPageUrl(),
                entity.getPageTitle(),
                entity.getCurrentActiveLocator(),
                entity.getLocatorVersion(),
                entity.getHealCount(),
                entity.getHealSuccessCount(),
                entity.getHealFailureCount(),
                entity.getAverageValidationScore(),
                entity.getLocatorConfidence(),
                entity.getAmbiguityScore(),
                entity.getDataTestId(),
                entity.getLastHealedAt(),
                entity.getLastValidatedAt(),
                history
        );
    }

    private HealingHistoryRecordDto toHistoryRecord(HealingHistoryEntity history) {
        if (history == null) {
            return null;
        }
        return new HealingHistoryRecordDto(
                history.getAttemptNumber(),
                history.getStatus(),
                history.getFailureReason(),
                history.getSelectedXpath(),
                history.getHealeniumXpath(),
                history.getHealeniumScore(),
                history.getCreatedAt()
        );
    }

    private int safeHealCount(Integer value) {
        return value == null ? 0 : value;
    }

    public boolean exists(String pageUrl, String locatorName) {
        return locatorRepo.findByPageUrlAndLocatorName(pageUrl, locatorName).isPresent();
    }

    private boolean isSameHash(String h1, String h2) {
        return h1 != null && h1.equals(h2);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
