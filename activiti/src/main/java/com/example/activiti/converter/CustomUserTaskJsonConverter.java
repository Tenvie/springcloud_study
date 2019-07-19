package com.example.activiti.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.CustomProperty;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.UserTaskJsonConverter;

import java.util.Map;

/**
 * @Class CustomUserTaskJsonConverter
 * @Author thz
 * @Date 2019/7/17
 * @Version 1.0
 */
public class CustomUserTaskJsonConverter extends UserTaskJsonConverter {

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        FlowElement flowElement = super.convertJsonToElement(elementNode, modelNode, shapeMap);
        UserTask userTask = (UserTask) flowElement;
        //将自己的属性添加到activiti自带的自定义属性中
        CustomProperty customProperty = new CustomProperty();
        customProperty.setName("process_test");
        customProperty.setSimpleValue(this.getPropertyValueAsString("process_test", elementNode));
        userTask.getCustomProperties().add(customProperty);
        CustomProperty customProperty2 = new CustomProperty();
        customProperty2.setName("multiTask");
        customProperty2.setSimpleValue(this.getPropertyValueAsString("multitask", elementNode));
        userTask.getCustomProperties().add(customProperty2);
        return userTask;
    }

    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        super.convertElementToJson(propertiesNode, baseElement);
    }

}
