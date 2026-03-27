package com.voltstream.billing_service;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface InvoiceRepository extends JpaRepository<InvoiceRecord, Long> {
    List<InvoiceRecord> findAllByUserId(String userId);
}