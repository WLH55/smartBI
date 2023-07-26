package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shier
 */
public class TtlConsumer {

    private final static String TTL_QUEUE = "ttl-queue";

    public static void main(String[] argv) throws Exception {
        // 建立链接，
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost("localhost");
//        factory.setUsername("xxxx.xxxx.xxx");
//        factory.setPassword("xxx.xxx.xxx");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 创建队列
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("x-message-ttl", 5000);
        // 指定args参数
        channel.queueDeclare(TTL_QUEUE, false, false, false, msg);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 定义如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        // 消费消息 autoAck设置为false 取消掉自动确认消息
        channel.basicConsume(TTL_QUEUE, false, deliverCallback, consumerTag -> { });
    }
}