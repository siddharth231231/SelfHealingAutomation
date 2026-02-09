package com.yourcompany.selfhealing.service;

import com.yourcompany.selfhealing.entity.ElementInteractionEntity;
import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.repository.ElementInteractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ElementInteractionService {

    private final ElementInteractionRepository interactionRepo;

    public ElementInteractionService(ElementInteractionRepository interactionRepo) {
        this.interactionRepo = interactionRepo;
    }

    /* ================= RECORD INTERACTION ================= */

    public ElementInteractionEntity recordInteraction(
            LocatorMetaEntity locator,
            String actionType,
            String navigationType,
            String beforeUri,
            String afterUri) {

        ElementInteractionEntity interaction = new ElementInteractionEntity();
        interaction.setLocator(locator);
        interaction.setActionType(actionType);
        interaction.setNavigationType(navigationType);
        interaction.setBeforeUri(beforeUri);
        interaction.setAfterUri(afterUri);

        // interactionTime is handled by @PrePersist
        return interactionRepo.save(interaction);
    }

    /* ================= ANALYSIS ================= */

    public List<ElementInteractionEntity> findByLocator(
            LocatorMetaEntity locator) {

        return interactionRepo.findByLocator(locator);
    }

    public List<ElementInteractionEntity> findByTimeRange(
            LocalDateTime from,
            LocalDateTime to) {

        return interactionRepo.findByInteractionTimeBetween(from, to);
    }
}
