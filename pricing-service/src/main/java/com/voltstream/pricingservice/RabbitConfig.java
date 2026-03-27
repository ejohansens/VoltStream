package com.voltstream.pricingservice;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange voltstreamExchange() {
        return new TopicExchange("voltstream-exchange");
    }

    // QUEUE 1: Listen for incoming energy
    @Bean
    public Queue pricingEnergyQueue() {
        return new Queue("pricing-energy-queue", true);
    }

    @Bean
    public Binding energyBinding(Queue pricingEnergyQueue, TopicExchange voltstreamExchange) {
        return BindingBuilder.bind(pricingEnergyQueue).to(voltstreamExchange).with("voltstream.energy.consumed");
    }

    // QUEUE 2: Listen for the stop signal
    @Bean
    public Queue pricingSessionStopQueue() {
        return new Queue("pricing-session-stop-queue", true);
    }

    @Bean
    public Binding sessionStopBinding(Queue pricingSessionStopQueue, TopicExchange voltstreamExchange) {
        return BindingBuilder.bind(pricingSessionStopQueue).to(voltstreamExchange).with("voltstream.session.stopped"); 
    }
}