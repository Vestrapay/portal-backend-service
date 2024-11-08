package com.example.vestrapay.merchant.settlements.repository;

import com.example.vestrapay.merchant.settlements.models.SettlementDurations;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface SettlementDurationRepository extends R2dbcRepository<SettlementDurations,Long> {
}
