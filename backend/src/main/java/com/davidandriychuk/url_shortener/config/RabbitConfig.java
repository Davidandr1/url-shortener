package com.davidandriychuk.url_shortener.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;


@Configuration
public class RabbitConfig {
    static final String topicExchangeName = "click-events-exchange";

    public static final String EXCHANGE = "click-events-exchange";
    public static final String QUEUE = "click-events";
    public static final String ROUTING_KEY = "click-event";


    @Bean
    Queue queue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}