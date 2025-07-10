package com.springboot.learning.kit.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.kit.event.OrderPlacedEvent;
import com.springboot.learning.kit.exception.OrderEventProducerException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    @Value("${amq.new.order.placed.topic}")
    public String virtualTopic;

    @Value("${rmq.topic.exchange}")
    private String topicExchange;

    @Value("${rmq.new.order.notification.routing.key}")
    private String rabbitMQQOrderNotificationRoutingKey;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ProducerTemplate producerTemplate;

    public void sendNewOrderNotificationToVirtualTopic(OrderPlacedEvent orderPlacedEvent){
        try{
            String message = objectMapper.writeValueAsString(orderPlacedEvent);

            String destination = "activemq:topic:" + virtualTopic;
            producerTemplate.sendBody(destination,message);
        }catch(Exception e){
            log.error("Failed to send new order notification to virtual topic", e);
            throw new OrderEventProducerException("Failed to send new order notification to virtual topic for order "
            + orderPlacedEvent.getOrderId() , e);
        }
    }

    public void sendNewOrderNotificationToRabbitMQQueue(OrderPlacedEvent orderPlacedEvent){
        try{
            String message = objectMapper.writeValueAsString(orderPlacedEvent);

            rabbitTemplate.convertAndSend(topicExchange, rabbitMQQOrderNotificationRoutingKey, message);
        }catch(Exception e){
            log.error("Failed to send new order notification to virtual topic", e);
            throw new OrderEventProducerException("Failed to send new order notification to virtual topic for order "
                    + orderPlacedEvent.getOrderId() , e);
        }
    }
}
