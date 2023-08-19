package com.yupi.springbootinit.handler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class MyDiscardPolicy implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        // 直接丢弃任务
        System.out.println("Task " + r.toString() + " rejected from " + e.toString());
    }
}