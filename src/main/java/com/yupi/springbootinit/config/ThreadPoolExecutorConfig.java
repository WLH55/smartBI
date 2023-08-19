package com.yupi.springbootinit.config;

import com.yupi.springbootinit.handler.MyDiscardPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        // 创建线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            //初始化线程数为1
            private int count = 1;
            //每当线程池需要创建新线程时，就会调用newThread方法
            //@NotNul1 Runnable r表示方法参数r应该永远不为nul1,
            //如果这个方法被调用的时候传递了一个nu11参数，就会报错
            @Override
            public Thread newThread(@NotNull Runnable r) {
                // 一定要将这个 r 放入到线程当中
                //创建一个新的线程
                Thread thread = new Thread(r);
                //给线程设置一个名字，名称中包含线程的序号
                thread.setName("线程：" + count);
                // 任务++  线程数递增
                count++;
                //返回新创建的线程
                return thread;
            }
        };
        // 创建线程池，线程池中的线程数最小为2，最大为4
        //非核心线程的空闲时间为100秒，超过这个时间就会被回收，任务队列采用阻塞队列，长度为100
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), threadFactory,new MyDiscardPolicy());
        //返回线程池
        return threadPoolExecutor;

    }
}