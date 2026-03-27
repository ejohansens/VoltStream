package com.voltstream.pricingservice;

import lombok.Data;
import java.io.Serializable;

@Data
public class PriceCalculatedEvent implements Serializable {
    private String sessionId;
    private String userId;
    private double totalEnergyCost;
}