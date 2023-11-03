package com.example.vestrapay.settlements.repository;

import com.example.vestrapay.settlements.models.SettlementDurations;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface SettlementDurationRepository extends R2dbcRepository<SettlementDurations,Long> {
}
