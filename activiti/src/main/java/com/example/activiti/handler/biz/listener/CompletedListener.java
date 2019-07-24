package com.example.activiti.handler.biz.listener;

import com.example.activiti.handler.AbstractHandler;
import com.example.activiti.handler.HandlerType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Class CompletedListener
 * @description: TODO
 * @Author thz
 * @Date 2019/7/22 17:05
 * @Version 1.0
 */
@Component("ACTIVITY_COMPLETED")
@HandlerType("ACTIVITY_COMPLETED")
public class CompletedListener extends AbstractHandler {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    HistoryService historyService;

    //解析器处理
    @Override
    public void handler(UserTask userTask, String text) {

    }

    //监听器处理
    @Override
    public void handler(ActivitiEvent event) {
        System.out.println("节点完成");
        // 取得当前任务
        List<HistoricTaskInstance> currTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(event.getProcessInstanceId()).list();
        if (currTask.size() > 0) {
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(currTask.get(0).getProcessDefinitionId()));
            // 取得当前活动
            ActivityImpl currActivity = definition.findActivity(currTask.get(0).getTaskDefinitionKey());
            List<PvmTransition> pvmTransitions = currActivity.getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitions) {
                ActivityImpl nextTask = (ActivityImpl)pvmTransition.getDestination();//获取所有的终点节点
            }
        }
    }
}
