package com.example.activiti.parseHandler;

import com.example.activiti.entity.PropertyEnum;
import com.example.activiti.utils.HandlerHelper;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.List;
import java.util.Map;

/**
 * @Class ExtensionUserTaskParseHandler
 * @Author thz
 * @Date 2019/7/17
 * @Version 1.0
 */
public class ExtensionUserTaskParseHandler extends UserTaskParseHandler {

    @Override
    protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
        //调用上层的解析
        super.executeParse(bpmnParse, userTask);
        //userTask有获取拓展元素的集合
        Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
        ActivityImpl activity = bpmnParse.getCurrentScope().findActivity(userTask.getId());
        for (String ex : extensionElements.keySet()) {
            for (ExtensionElement e : extensionElements.get(ex)) {
                //把key即自定义属性名和value（自定义属性值）存入activity的属性中
                activity.setProperty(ex, e.getElementText());
                //自定义属性的处理
                //需要处理器的
                if (PropertyEnum.list().contains(ex)) {
                    HandlerHelper.handle(ex, userTask, e.getElementText());
                } else {
                    //直接保存数据库的
                }
            }
        }
    }
}
