package com.example.activiti.listener;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.List;

/**
 * @Class ProcessEventListener
 * @description: TODO
 * @Author thz
 * @Date 2019/7/18 14:19
 * @Version 1.0
 */
public class ProcessEventListener implements ActivitiEventListener {
    @Override
    public void onEvent(ActivitiEvent event) {
        ActivitiEventType eventType = event.getType();

        if(ActivitiEventType.ACTIVITY_STARTED.equals(eventType)){
            System.out.println("流程启动");
        }
    }
    @Override
    public boolean isFailOnException() {
        return false;
    }
}
