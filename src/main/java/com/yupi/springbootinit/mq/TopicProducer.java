package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.Scanner;


/**
 * @author WLH
 * 主题消费者 - 生产者
 */
public class TopicProducer {

  private static final String TOPIC_EXCHANGE = "topic-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost("localhost");
//        factory.setUsername("xxxx.xxxx.xxx");
//        factory.setPassword("xxx.xxx.xxx");

    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {

        channel.exchangeDeclare(TOPIC_EXCHANGE, "topic");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String userInput = scanner.nextLine();
            String[] splits = userInput.split(" ");
            if (splits.length < 1) {
                continue;
            }
            String message = splits[0];
            String routingKey = splits[1];

            channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + " with routing " + routingKey + "'");
        }
    }
  }
}