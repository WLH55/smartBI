package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author Shier
 */
public class TtlProducer {
    private final static String TTL_QUEUE = "ttl-queue";

    public static void main(String[] argv) throws Exception {
        // 创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost("localhost");
//        factory.setUsername("xxxx.xxxx.xxx");
//        factory.setPassword("xxx.xxx.xxx");
        try (Connection connection = factory.newConnection();
             // 建立链接，创建频道
             Channel channel = connection.createChannel()) {
            // 创建消息队列 要删除掉，因为已经在消费者中创建了队列，没有必要再重新创建一次这个队列，如果在此处还创建队列，里面的参数必须要和消费者的参数一致
            // channel.queueDeclare(SINGLE_QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", TTL_QUEUE, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}