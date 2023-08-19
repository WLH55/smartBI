package com.yupi.springbootinit.bzimq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.BiMqConstant;
import com.yupi.springbootinit.constant.ChartConstant;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManger;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.enums.ChartStatus;
import com.yupi.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * rabbitmq消息消费者
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;
    @Resource
    private AiManger aiManger;

    /**
     * 指定程序监听的消息队列和确认机制
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @RabbitListener(queues = { BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    private void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
      log.info("receiveMessage = {}",message);

            if(StringUtils.isBlank(message)){
                //拒接消息
                channel.basicNack(deliveryTag,false,false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
            }
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if(chart == null){
                channel.basicNack(deliveryTag,false,false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表不存在");
            }
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(ChartStatus.RUNNING.getValue());
            boolean b = chartService.updateById(updateChart);
            if(!b){
                channel.basicNack(deliveryTag,false,false);
                handleChartUpdateError(chart.getId(),"更新图表状态为running失败");
                return;
            }


            //调用AI接口
            String result = aiManger.doChatByAi(CommonConstant.AI_MODEL, buildUserInput(chart));
            String[] splits = result.split(ChartConstant.GEN_CONTENT_SPLITS);
            if (splits.length < ChartConstant.GEN_ITEM_NUM) {
                channel.basicNack(deliveryTag, false, false);
                handleChartUpdateError(chart.getId(), "AI生成失败");
                return;
            }
            String chartCode = splits[ChartConstant.GEN_CHART_IDX].trim();
            String chartResult = splits[ChartConstant.GEN_RESULT_IDX].trim();
            //调用AI得到结果后，更新图表状态
            Chart updateChart2 = new Chart();
            updateChart2.setId(chart.getId());
            updateChart2.setGenChart(chartCode);
            updateChart2.setGenResult(chartResult);
            updateChart2.setStatus(ChartStatus.SUCCEED.getValue());

            boolean update2 = chartService.updateById(updateChart2);
            if (!update2) {
                channel.basicNack(deliveryTag, false, false);
                handleChartUpdateError(chart.getId(), "更新图标状态为succeed失败");
            }
            // 手动确认消息
            channel.basicAck(deliveryTag,false);

    }

    private String buildUserInput(Chart chart){
        String goal = chart.getGoal();

        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal =  "请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    //上面的接口用到很多异常，直接定义一个工具
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus(ChartStatus.FAILED.getValue());
        updateChart.setExecMessage(execMessage);
        boolean update = chartService.updateById(updateChart);
        if (!update) {
            log.error("图表状态更新失败" + chartId + "," + execMessage);
        }
    }
}