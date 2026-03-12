package com.voltstream.pricingservice;

import lombok.Data;
import java.io.Serializable;

@Data
public class EnergyConsumedEvent implements Serializable {
    private String sessionId;
    private double kwhDelta; // The amount of energy sent by Ivan's service
}