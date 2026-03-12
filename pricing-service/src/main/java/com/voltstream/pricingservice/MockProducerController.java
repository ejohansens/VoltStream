package com.voltstream.pricingservice;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/mock")
public class MockProducerController {

    @Autowired 
    private RabbitTemplate rabbitTemplate;

    @Autowired 
    private PricingRepository repository;

    /**
     * SIMULATES IVAN (Session Service)
     * This endpoint sends a message to RabbitMQ.
     * URL: http://localhost:8080/mock/send-energy?id=SESS-001&kwh=0.5
     */
    @GetMapping("/send-energy")
    public String sendEnergy(@RequestParam String id, @RequestParam double kwh) {
        // 1. Create the event object
        EnergyConsumedEvent event = new EnergyConsumedEvent();
        event.setSessionId(id);
        event.setKwhDelta(kwh);

        // 2. Send it to the "Sorting Center" (Exchange) with the "Label" (Routing Key)
        // This matches the hierarchical naming we set up in RabbitConfig
        rabbitTemplate.convertAndSend(
            "voltstream-exchange", 
            "voltstream.energy.consumed", 
            event
        );

        return "SUCCESS: Sent " + kwh + " kWh update to RabbitMQ for Session " + id;
    }

    /**
     * SIMULATES THE BILLING SERVICE
     * This endpoint looks in the Database and sums up all costs for a session.
     * URL: http://localhost:8080/mock/get-bill?id=SESS-001
     */
    @GetMapping("/get-bill")
    public String getBill(@RequestParam String id) {
        // 1. Find all records in the DB for this specific ID
        List<PricingRecord> records = repository.findAllBySessionId(id);

        if (records.isEmpty()) {
            return "No data found for Session: " + id;
        }

        // 2. Sum up the 'cost' field from all those records
        double totalBill = records.stream()
                                  .mapToDouble(PricingRecord::getCost)
                                  .sum();

        return String.format(
            "--- FINAL INVOICE ---\n" +
            "Session ID: %s\n" +
            "Number of Energy Updates: %d\n" +
            "TOTAL AMOUNT TO PAY: $%.2f", 
            id, records.size(), totalBill
        );
    }
    @GetMapping("/stop-session")
    public String stopSession(@RequestParam String id, @RequestParam String user) {
        // Just send the "Stop" event. The BillingService will do the rest.
        SessionStoppedEvent event = new SessionStoppedEvent();
        event.setSessionId(id);
        event.setUserId(user);

        rabbitTemplate.convertAndSend("voltstream-exchange", "voltstream.session.stopped", event);

        return "MOCK: Session Stop signal sent to Broker for " + id;
    }
}