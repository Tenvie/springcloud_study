package com.example.activiti.converter;

import com.example.activiti.entity.PropertyEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.CustomProperty;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.UserTaskJsonConverter;
import org.activiti.engine.impl.json.JsonProcessDefinitionConverter;

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
        for(String property : PropertyEnum.all()) {
            CustomProperty customProperty = new CustomProperty();
            customProperty.setName(property);
            customProperty.setSimpleValue(this.getPropertyValueAsString(property.toLowerCase(), elementNode));
            userTask.getCustomProperties().add(customProperty);
        }
        //设置流程实例属性
        CustomProperty processProperty = new CustomProperty();
        processProperty.setName("process");
        processProperty.setSimpleValue(modelNode.get("properties").toString());
        userTask.getCustomProperties().add(processProperty);
        return userTask;
    }

    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        super.convertElementToJson(propertiesNode, baseElement);
    }

}
