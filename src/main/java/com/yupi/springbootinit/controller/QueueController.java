package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManger;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 队列测试
 */
@RestController
@RequestMapping("/queue")
@Profile("dev,local")
@Slf4j
public class QueueController {

    //  自动注入一个线程池的实例
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @GetMapping("/add")
    public void add(String name){
        //使用CompletableFuture.runAsync()方法执行一个异步任务
        CompletableFuture.runAsync(() -> {
            //打印一条日志，包括任务名称和执行线程的名字
            log.info("异步任务执行中"+name +",执行人"+ Thread.currentThread().getName());
            try{
                // 休眠10分钟，模拟长时间运行的任务
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //异步任务在threadPoolExecutor线程池中执行
        },threadPoolExecutor);
    }
    @GetMapping("/get")
    //该方法返回线程池的状态信息
    public String get(){
        //创建一个map对象，用于存放线程池的状态信息
        Map<String,Object> map = new HashMap<>();
        //获取线程池的队列长度
        int size = threadPoolExecutor.getQueue().size();
        // 将队列长度放入map中
        map.put("队列长度",size);
        //获取线程池已接收任务的数量
        long taskCount = threadPoolExecutor.getTaskCount();
        //将任务总数放入map中
        map.put("任务总数",taskCount);
        //获取线程池已完成任务的数量
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        //将已完成任务数放入map中
        map.put("已完成任务数",completedTaskCount);
        //获取正在执行任务的线程数
        int activeCount = threadPoolExecutor.getActiveCount();
        //将正在执行任务的线程数放入map中
        map.put("活跃线程数",activeCount);
        //将map转成json字符串并返回
        return JSONUtil.toJsonStr(map);

    }

}
