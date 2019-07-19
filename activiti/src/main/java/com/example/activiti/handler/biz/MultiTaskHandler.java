package com.example.activiti.handler.biz;


import com.example.activiti.handler.AbstractHandler;
import com.example.activiti.handler.ExtenProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Class MultiTaskHandler
 * @description: TODO
 * @Author thz
 * @Date 2019/7/18 15:03
 * @Version 1.0
 */
@Component("multiTask")
@ExtenProperty("multiTask")
public class MultiTaskHandler extends AbstractHandler {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    TaskService taskService;

    @Override
    public void handler(UserTask userTask, String text) {
        if ("true".equals(text)) {
                // 获取多实例配置
                MultiInstanceLoopCharacteristics characteristics = new MultiInstanceLoopCharacteristics();
                // 设置为同时接收（false 表示不按顺序执行）
                characteristics.setSequential(false);
                // 设置条件（暂时处理成，全部会签完转下步）
                characteristics.setCompletionCondition("${nrOfCompletedInstances==nrOfInstances}");

                userTask.setLoopCharacteristics(characteristics);
        }
    }
}
