package com.voltstream.pricingservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


// This is a simple controller we can use to simulate the Charger and Ivan/Sashas Service sending messages to our Pricing Service.
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 1. Simulate the Charger sending energy updates
    @GetMapping("/energy")
    public String sendEnergy(@RequestParam String id, @RequestParam double kwh) {
        EnergyConsumedEvent event = new EnergyConsumedEvent();
        event.setSessionId(id);
        event.setKwhDelta(kwh);
        
        rabbitTemplate.convertAndSend("voltstream-exchange", "voltstream.energy.consumed", event);
        return "TEST SUCCESS: Sent " + kwh + " kWh update for Session " + id;
    }

    // 2. Simulate their Service stopping the session
    @GetMapping("/stop")
    public String stopSession(@RequestParam String id, @RequestParam String user) {
        SessionStoppedEvent event = new SessionStoppedEvent();
        event.setSessionId(id);
        event.setUserId(user);
        
        rabbitTemplate.convertAndSend("voltstream-exchange", "voltstream.session.stopped", event);
        return "TEST SUCCESS: Sent Stop signal for Session " + id;
    }

    // 3. Catch the final invoice from the Billing Service so we can see it!
    //Small deserialization to get the details
    public static class DummyInvoice {
        public String sessionId;
        public String userId;
        public double totalAmount;
        public String status;
    }

    @RabbitListener(bindings = @org.springframework.amqp.rabbit.annotation.QueueBinding(
        value = @org.springframework.amqp.rabbit.annotation.Queue(value = "test-invoice-queue", durable = "true"),
        exchange = @org.springframework.amqp.rabbit.annotation.Exchange(value = "voltstream-exchange", type = "topic"),
        key = "voltstream.invoice.ready"
    ))
    public void catchFinalInvoice(DummyInvoice invoice) {
        System.out.println("\nTEST HARNESS CAUGHT FINAL INVOICE");
        System.out.println("Session ID: " + invoice.sessionId);
        System.out.println("User ID: " + invoice.userId);
        System.out.println(String.format("Final Amount to Pay: EUR%.2f", invoice.totalAmount));
        System.out.println("Status: " + invoice.status);
        System.out.println("----------------------------------------------\n");
    }
}