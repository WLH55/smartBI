package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author WLH
 * @verstion 1.0
 */
@SpringBootTest
class AiMangerTest {
    @Resource
    private AiManger aiManger;

    @Test
    void doChatByAi() {
        String s = aiManger.doChatByAi(1683130877963513857L,"分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期，用户数\n" +
                "1号，10\n" +
                "2号，20\n" +
                "3号，30");
        System.out.println(s);

    }
}