package com.voltstream.billing_service;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class InvoiceRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sessionId;
    private String userId;
    
    private double rawEnergyCost;
    private double staticFee;
    private double totalAmount;
    
    private String status; 
    private LocalDateTime createdAt;
}