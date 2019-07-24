package com.example.activiti.handler;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;

/**
 * @Class AbstractHandler
 * @description: TODO
 * @Author thz
 * @Date 2019/7/18 14:58
 * @Version 1.0
 */
public abstract class AbstractHandler {

    abstract public void handler(UserTask userTask, String text,BpmnParse bpmnParse);

    abstract public void handler(ActivitiEvent event);
}
