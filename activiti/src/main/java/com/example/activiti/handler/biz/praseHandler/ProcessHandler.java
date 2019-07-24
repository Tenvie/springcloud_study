package com.example.activiti.handler.biz.praseHandler;

import com.example.activiti.dao.CustomInstanceDao;
import com.example.activiti.entity.CustomInstanceEntity;
import com.example.activiti.handler.AbstractHandler;
import com.example.activiti.handler.HandlerType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

/**
 * @Class ProcessHandler
 * @description: TODO
 * @Author thz
 * @Date 2019/7/24 14:08
 * @Version 1.0
 */
@Component("process")
@HandlerType("process")
public class ProcessHandler extends AbstractHandler {

    @Resource(name = CustomInstanceDao.BEAN_NAME)
    CustomInstanceDao customInstanceDao;

    @Override
    public void handler(UserTask userTask, String text, BpmnParse bpmnParse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode process = mapper.readTree(text);
            CustomInstanceEntity customInstanceEntity = new CustomInstanceEntity();
            customInstanceEntity.setProcessId(process.get("process_id").textValue());
            customInstanceEntity.setFormId(process.get("formid").textValue());
            customInstanceEntity.setManagers(process.get("managers").textValue());
            //存储数据库
            customInstanceDao.save(customInstanceEntity);
        } catch (IOException ec) {
            ec.printStackTrace();
        }
    }

    @Override
    public void handler(ActivitiEvent event) {

    }
}
