//package com.yupi.springbootinit.bzimq;
//
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
///**
// * rabbitmq消息生产者
// */
//
//@Component
//public class RabbitMqMessageProducer {
//
//    @Resource
//    private RabbitTemplate rabbitTemplate;
//
//    /**
//     * 发送消息
//     * @param exchange
//     * @param routingKey
//     * @param message
//     */
//    public void sendMessage(String exchange, String routingKey, String message) {
//        rabbitTemplate.convertAndSend(exchange, routingKey, message);
//    }
//}