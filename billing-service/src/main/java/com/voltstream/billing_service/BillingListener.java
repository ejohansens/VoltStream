package com.voltstream.billing_service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

// 
@Service
public class BillingListener {

    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private InvoiceRepository invoiceRepository; 

    @RabbitListener(queues = "billing-queue")
    public void generateInvoice(PriceCalculatedEvent event) {
        
        double staticFee = 3.00; //Change this according to what we decide
        double finalGrandTotal = Math.max(0.0, event.getTotalEnergyCost() + staticFee);

        // 1. Save record to DB
        InvoiceRecord record = new InvoiceRecord();
        record.setSessionId(event.getSessionId());
        record.setUserId(event.getUserId());
        record.setRawEnergyCost(event.getTotalEnergyCost());
        record.setStaticFee(staticFee);
        record.setTotalAmount(finalGrandTotal);
        record.setStatus("UNPAID");
        record.setCreatedAt(LocalDateTime.now());
        
        invoiceRepository.save(record);

        // 2. Create and publish the InvoiceReadyEvent to notify other services
        InvoiceReadyEvent invoiceEvent = new InvoiceReadyEvent();
        invoiceEvent.setSessionId(event.getSessionId());
        invoiceEvent.setUserId(event.getUserId());
        invoiceEvent.setTotalAmount(finalGrandTotal);

        rabbitTemplate.convertAndSend("voltstream-exchange", "voltstream.invoice.ready", invoiceEvent);

        System.out.println(String.format("BILLING SERVICE: Saved to DB & Generated Invoice for Session %s. Total: %.2f EUR", 
                           event.getSessionId(), finalGrandTotal));
    }
}