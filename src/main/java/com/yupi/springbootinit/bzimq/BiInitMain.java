package com.yupi.springbootinit.bzimq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yupi.springbootinit.constant.BiMqConstant;

import java.util.HashMap;
import java.util.Map;

public class BiInitMain  {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
        	// 设置 rabbitmq 对应的信息
       	 	factory.setHost("47.113.186.223");
        	factory.setUsername("guest");
        	factory.setPassword("guest");

            // 创建与 RabbitMQ 服务器的连接
            Connection connection = factory.newConnection();

            // 创建一个通道
            Channel channel = connection.createChannel();

            String BI_EXCHANGE_NAME = BiMqConstant.BI_EXCHANGE_NAME;
            String BI_EXCHANGE_DEAD = BiMqConstant.BI_EXCHANGE_DEAD;
            // 声明一个直连交换机，一个死信交换机（其实就是普通交换机）
            channel.exchangeDeclare(BI_EXCHANGE_NAME, "direct");
            channel.exchangeDeclare(BI_EXCHANGE_DEAD, "direct");
            // 创建一个队列，随机分配一个队列名称
            String queueName = BiMqConstant.BI_QUEUE_NAME;
            // 通过设置 x-message-ttl 参数来指定消息的过期时间
            Map<String, Object> queueArgs = new HashMap<>();
            queueArgs.put("x-message-ttl", 60000); // 过期时间为 60 秒
            // 参数解释：queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
            // durable: 持久化队列（重启后依然存在）
            // exclusive: 排他性队列（仅限此连接可见，连接关闭后队列删除）
            // autoDelete: 自动删除队列（无消费者时自动删除）
            channel.queueDeclare(queueName, true, false, false, queueArgs);

            String deadLetterRoutingKey = ""; // 空字符串，表示所有过期消息都会路由到死信交换机
            Map<String, Object> deadArgs = new HashMap<>();
            deadArgs.put("x-dead-letter-exchange", BI_EXCHANGE_DEAD);
            deadArgs.put("x-dead-letter-routing-key", deadLetterRoutingKey);
            // 将队列与交换机进行绑定
            channel.queueBind(queueName, BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY,deadArgs);

            // 创建一个死信队列
            String queueDeadName = BiMqConstant.BI_QUEUE_DEAD;
            // 声明私信队列，并将其绑定到私信交换机。
            channel.queueDeclare(queueDeadName,true,false,false,null);
            channel.queueBind(queueDeadName,BI_EXCHANGE_DEAD,deadLetterRoutingKey);


        }catch (Exception e){
            
        }
    }

}