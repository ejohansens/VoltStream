package com.voltstream.pricingservice;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class GridPriceService {
    private final RestTemplate restTemplate = new RestTemplate();

    public double fetchCurrentPrice() {
        // 1. Get Current Time in UTC (format: 2023-10-27T10:00:00Z)
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        
        // We want the price for today, so we fetch from the start of today to the end of today
        String fromDate = now.truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_INSTANT);
        String tillDate = now.truncatedTo(ChronoUnit.DAYS).plusDays(1).format(DateTimeFormatter.ISO_INSTANT);

        // 2. Build the Dynamic URL
        String url = String.format(
            "https://api.energyzero.nl/v1/energyprices?fromDate=%s&tillDate=%s&interval=4&usageType=1",
            fromDate, tillDate
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> prices = (List<Map<String, Object>>) response.get("Prices");

            // 3. Find the price for the CURRENT hour
            int currentHour = now.getHour();
            
            // EnergyZero returns 24 prices (one for each hour). 
            // We pick the one matching the current hour index.
            if (prices != null && prices.size() > currentHour) {
                return Double.parseDouble(prices.get(currentHour).get("price").toString());
            }
            
            return 0.25; // Fallback if list is empty
        } catch (Exception e) {
            System.err.println("Pricing Error: " + e.getMessage());
            return 0.22; // Hardcoded fallback if API is down
        }
    }
}