package com.voltstream.pricingservice;

import lombok.Data;
import java.io.Serializable;

@Data
public class SessionStoppedEvent implements Serializable {
    private String sessionId;
    private String userId;
    
}