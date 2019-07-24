package com.example.activiti.handler.biz.listener;

import com.example.activiti.handler.AbstractHandler;
import com.example.activiti.handler.HandlerType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.springframework.stereotype.Component;

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
    public void handler(UserTask userTask, String text) {

    }

    //监听器处理
    @Override
    public void handler(ActivitiEvent event) {
        //更新任务状态为已挂起
        //计算已挂起时间
        System.out.printf("流程已挂起");
    }
}
