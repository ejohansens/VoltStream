package com.voltstream.pricingservice;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // ADD THIS BEAN:
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange voltstreamExchange() {
        return new TopicExchange("voltstream-exchange");
    }

    @Bean
    public Queue pricingEnergyQueue() {
        return new Queue("pricing-energy-queue", true);
    }

    @Bean
    public Binding energyBinding(Queue pricingEnergyQueue, TopicExchange voltstreamExchange) {
        return BindingBuilder.bind(pricingEnergyQueue).to(voltstreamExchange).with("voltstream.energy.consumed");
    }
    @Bean
    public Queue sessionNotificationQueue() {
        return new Queue("session-notification-queue", true);
    }

    @Bean
    public Binding sessionBinding(Queue sessionNotificationQueue, TopicExchange voltstreamExchange) {
        return BindingBuilder
                .bind(sessionNotificationQueue)
                .to(voltstreamExchange)
                .with("voltstream.invoice.ready"); // The label Ivan will listen for
    }
    @Bean
    public Queue billingQueue() {
        return new Queue("billing-queue", true);
    }

    @Bean
    public Binding billingBinding(Queue billingQueue, TopicExchange voltstreamExchange) {
        return BindingBuilder
                .bind(billingQueue)
                .to(voltstreamExchange)
                .with("voltstream.session.stopped"); // Road for the Stop signal
    }
}