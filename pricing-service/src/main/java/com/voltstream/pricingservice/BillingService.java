package com.voltstream.pricingservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BillingService {

    @Autowired private PricingRepository repository;
    @Autowired private RabbitTemplate rabbitTemplate;

    // 1. Listen for the "Stop" signal from Ivan
    @RabbitListener(queues = "billing-queue")
    public void processFinalBill(SessionStoppedEvent event) {
        List<PricingRecord> records = repository.findAllBySessionId(event.getSessionId());
        
        double energyTotal = records.stream().mapToDouble(PricingRecord::getCost).sum();
        double staticFee = 3.00;
        double finalGrandTotal = Math.max(0.0, energyTotal + staticFee); // Safeguard

        InvoiceReadyEvent invoice = new InvoiceReadyEvent();
        invoice.setSessionId(event.getSessionId());
        invoice.setUserId(event.getUserId());
        invoice.setTotalAmount(finalGrandTotal);

        rabbitTemplate.convertAndSend("voltstream-exchange", "voltstream.invoice.ready", invoice);
        
        // PRO TIP: Clear the database for this session so you don't sum old data next time
        repository.deleteAll(records);

        System.out.println(String.format("BILLING SERVICE: Finalized Session %s. Total: %.2f EUR", 
                           event.getSessionId(), finalGrandTotal));
    }
}