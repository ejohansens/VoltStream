package com.voltstream.pricingservice;

import lombok.Data;
import java.io.Serializable;

@Data
public class SessionStoppedEvent implements Serializable {
    private String sessionId;
    private String userId;
    
    // In a real system, you might also add the final meter reading here, 
    // but for our mock, the Billing Service will just look in the DB.
}