package com.voltstream.pricingservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PricingListener {

    @Autowired private GridPriceService priceService;
    @Autowired private PricingRepository repository;
    @Autowired private RabbitTemplate rabbitTemplate; // Needed to send messages to Billing

    @RabbitListener(queues = "pricing-energy-queue") 
    public void onEnergyConsumed(EnergyConsumedEvent event) {
        double rawGridPrice = priceService.fetchCurrentPrice();
        
        double energyCost = event.getKwhDelta() * rawGridPrice;

        PricingRecord record = new PricingRecord();
        record.setSessionId(event.getSessionId());
        record.setGridPrice(rawGridPrice);
        record.setCost(energyCost);
        repository.save(record);

        //Just some comments for testing, can remove later
        System.out.println("PRICING SERVICE: ENERGY UPDATE");
        System.out.println("Session ID: " + event.getSessionId());
        System.out.println(String.format("Grid Price: %.5f EUR/kWh", rawGridPrice));
        System.out.println(String.format("Energy Cost for segment: EUR%.5f", energyCost));
      
    }

    // Listen for stop, calculate total, send to Billing
    @RabbitListener(queues = "pricing-session-stop-queue")
    public void onSessionStopped(SessionStoppedEvent event) {
        List<PricingRecord> records = repository.findAllBySessionId(event.getSessionId());
        
        double totalEnergyCost = records.stream()
                                        .mapToDouble(PricingRecord::getCost)
                                        .sum();

        PriceCalculatedEvent priceEvent = new PriceCalculatedEvent();
        priceEvent.setSessionId(event.getSessionId());
        priceEvent.setUserId(event.getUserId());
        priceEvent.setTotalEnergyCost(totalEnergyCost);

        rabbitTemplate.convertAndSend("voltstream-exchange", "voltstream.price.calculated", priceEvent);

        repository.deleteAll(records);

        System.out.println("PRICING SERVICE: Session " + event.getSessionId() + " stopped. Calculated total raw energy cost: EUR" + totalEnergyCost + ". Sent to Billing.");
    }
}