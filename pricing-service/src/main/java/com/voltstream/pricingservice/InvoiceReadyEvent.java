package com.voltstream.pricingservice;

import lombok.Data;
import java.io.Serializable;

@Data
public class InvoiceReadyEvent implements Serializable {
    private String sessionId;
    private String userId;
    private double totalAmount;
    private String status = "PAID";
}