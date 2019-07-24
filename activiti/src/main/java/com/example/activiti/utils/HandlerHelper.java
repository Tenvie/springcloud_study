package com.example.activiti.utils;

import com.example.activiti.handler.HandlerContext;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Class PropertyUtils
 * @description: TODO
 * @Author thz
 * @Date 2019/7/19 9:21
 * @Version 1.0
 */
@Component
public class HandlerHelper {

    @Autowired
    private HandlerContext handlerContext;

    private static HandlerHelper handlerHelper;

    //初始化
    @PostConstruct
    public void init() {
        handlerHelper = this;
        handlerHelper.handlerContext = this.handlerContext;
    }

    public static void handle(String type, UserTask userTask, String text) {
        // 调用方法
        handlerHelper.handlerContext.getInstance(type).handler(userTask, text);
    }

    public static void handle(String type, ActivitiEvent event) {
        // 调用方法
        handlerHelper.handlerContext.getInstance(type).handler(event);
    }
}
