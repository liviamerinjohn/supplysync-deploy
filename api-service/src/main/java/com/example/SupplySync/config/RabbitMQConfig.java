package com.example.SupplySync.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME ="job-processing-queue";
    public static final String EXCHANGE_NAME ="job-exchange";
    public static final String ROUTING_KEY ="job.processing";
    /*
    ROUTING_KEY — the "address label" a message needs to match this binding.
     We made it a public static final String constant so we can reuse the exact
     same value when publishing messages later,
     instead of retyping the string and risking a typo mismatch.
    */
    @Bean
    public Queue jobQueue() {
        return new Queue(QUEUE_NAME, true);  // true = durable (survives RabbitMQ restart)
    }
    @Bean
    public DirectExchange jobExchange() {
        return new DirectExchange(EXCHANGE_NAME);  //DirectExchange: one specific type of exchange; it routes messages to a queue based on an exact match of a "routing key"
    }

    //There are other exchange types (TopicExchange, FanoutExchange) for more complex routing, but DirectExchange is the simplest and more suitable for present use case.

    @Bean
    public Binding binding(Queue jobQueue, DirectExchange jobExchange) {
        return BindingBuilder.bind(jobQueue).to(jobExchange).with(ROUTING_KEY);
    }
}
