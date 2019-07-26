package com.example.activiti.service;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Class CustomRunTimeService
 * @description: RunTimeService封装类
 * @Author thz
 * @Date 2019/7/26 15:31
 * @Version 1.0
 */
@Component
public class CustomRunTimeService {

    @Autowired
    RuntimeService runtimeService;

    /**
     * 跳转至任意节点
     *
     * @param processId 流程实例id
     * @param targetKey 目标节点key（流程定义中的唯一标识）
     * @param reason    跳转原因，用于拿到跳转节点
     * @return
     */
    public void reachActivity(String processId, String targetKey, String reason) {
        ((RuntimeServiceImpl) runtimeService).getCommandExecutor().execute(commandContext -> {
            ExecutionEntity execution = commandContext.getExecutionEntityManager().findExecutionById(processId);
            execution.destroyScope(reason);
            ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
            ActivityImpl findActivity = processDefinition.findActivity(targetKey);
            execution.executeActivity(findActivity);
            return execution;
        });
    }
}
