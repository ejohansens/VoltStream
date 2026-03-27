package com.voltstream.billing_service;

import lombok.Data;
import java.io.Serializable;

@Data
public class PriceCalculatedEvent implements Serializable {
    private String sessionId;
    private String userId;
    private double totalEnergyCost;
}