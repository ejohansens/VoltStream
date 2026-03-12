package com.voltstream.pricingservice;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PricingRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sessionId;
    private double cost;
    private double gridPrice;
}