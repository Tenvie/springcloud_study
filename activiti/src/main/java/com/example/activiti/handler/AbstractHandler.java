package com.example.activiti.handler;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;

/**
 * @Class AbstractHandler
 * @description: TODO
 * @Author thz
 * @Date 2019/7/18 14:58
 * @Version 1.0
 */
public abstract class AbstractHandler {

    abstract public void handler(UserTask userTask, String text);
}
