package com.yupi.springbootinit.utils;

import com.yupi.springbootinit.bzimq.RabbitMqMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RabbitMqMessageProducerTest {


    @Resource
    private RabbitMqMessageProducer messageProducer;


    @Test
    void sendMessage() {
        messageProducer.sendMessage("demo_exchange","demo_routingKey","欢迎来到十二智能BI系统");
    }
}