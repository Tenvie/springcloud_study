package com.example.activiti.handler.biz.listener;

import com.example.activiti.handler.AbstractHandler;
import com.example.activiti.handler.HandlerType;
import com.example.activiti.scheduler.WorkflowScheduler;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * @Class SuspendListener
 * @description: TODO
 * @Author thz
 * @Date 2019/7/23 15:22
 * @Version 1.0
 */
@Component("ENTITY_SUSPENDED")
@HandlerType("ENTITY_SUSPENDED")
public class SuspendListener extends AbstractHandler {
    //解析器处理
    @Override
    public void handler(UserTask userTask, String text,BpmnParse bpmnParse) {

    }
    @Autowired
    WorkflowScheduler workflowScheduler;

    //监听器处理
    @Override
    public void handler(ActivitiEvent event) {
        //更新任务状态为已挂起
        //计算已挂起时间
        System.out.printf("流程已挂起");
        //workflowScheduler.setCron("0/5 * * * * ?");

        System.out.println("开启任务计时定时任务");
    }
}
