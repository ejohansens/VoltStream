package com.voltstream.billing_service;

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

    @Bean
    public Queue billingQueue() {
        return new Queue("billing-queue", true);
    }

    @Bean
    public Binding billingBinding(Queue billingQueue, TopicExchange voltstreamExchange) {
        return BindingBuilder.bind(billingQueue).to(voltstreamExchange).with("voltstream.price.calculated");
    }
}