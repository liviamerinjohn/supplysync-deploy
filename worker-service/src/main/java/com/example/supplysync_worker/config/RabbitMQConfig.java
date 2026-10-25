package com.example.supplysync_worker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "job-processing-queue";
    public static final String EXCHANGE_NAME = "job-exchange";
    public static final String ROUTING_KEY = "job.processing";

    @Bean
    public Queue jobQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange jobExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue jobQueue, DirectExchange jobExchange) {
        return BindingBuilder.bind(jobQueue).to(jobExchange).with(ROUTING_KEY);
    }
}