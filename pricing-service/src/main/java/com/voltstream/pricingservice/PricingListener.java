package com.voltstream.pricingservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PricingListener {

    @Autowired private GridPriceService priceService;
    @Autowired private PricingRepository repository;

    @RabbitListener(queues = "pricing-energy-queue") 
    public void onEnergyConsumed(EnergyConsumedEvent event) {
        double rawGridPrice = priceService.fetchCurrentPrice();
        
        // Calculate RAW energy cost (Grid Price only)
        double energyCost = event.getKwhDelta() * rawGridPrice;

        // Save to DB
        PricingRecord record = new PricingRecord();
        record.setSessionId(event.getSessionId());
        record.setGridPrice(rawGridPrice);
        record.setCost(energyCost);
        repository.save(record);

        System.out.println("================================");
        System.out.println("PRICING SERVICE: ENERGY UPDATE");
        System.out.println("Session ID: " + event.getSessionId());
        System.out.println(String.format("Grid Price: %.5f EUR/kWh", rawGridPrice));
        System.out.println(String.format("Energy Cost for segment: EUR%.5f", energyCost));
        System.out.println("================================");
    }

    // This simulates Ivan's service receiving the final bill
    @RabbitListener(queues = "session-notification-queue")
    public void onInvoiceReceived(InvoiceReadyEvent event) {
        System.out.println("\n--- MESSAGE RECEIVED BY SESSION SERVICE ---");
        System.out.println("Confirmation for User: " + event.getUserId());
        System.out.println(String.format("Final Bill: EUR%.2f (Includes EUR3.00 static fee)", event.getTotalAmount()));
        System.out.println("Session " + event.getSessionId() + " is now officially CLOSED.");
        System.out.println("-------------------------------------------\n");
    }
}