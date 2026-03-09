package com.yourcompany.selfhealing.repository;

import com.yourcompany.selfhealing.entity.HealingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealingHistoryRepository extends JpaRepository<HealingHistoryEntity, Long> {

    List<HealingHistoryEntity> findByLocatorNameOrderByCreatedAtDesc(String locatorName);
}
