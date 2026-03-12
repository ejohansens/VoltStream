package com.voltstream.pricingservice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PricingRepository extends JpaRepository<PricingRecord, Long> {
    List<PricingRecord> findAllBySessionId(String sessionId);
}