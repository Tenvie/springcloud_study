package com.example.activiti.config;

import com.example.activiti.listener.ProcessEventListener;
import com.example.activiti.parseHandler.ExtensionUserTaskParseHandler;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @Class ActivitiConfig
 * @description: TODO
 * @Author thz
 * @Date 2019/7/17 15:30
 * @Version 1.0
 */
@Configuration
public class ActivitiConfig implements ProcessEngineConfigurationConfigurer {
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");

        processEngineConfiguration.setDbIdentityUsed(false);
        processEngineConfiguration.setDatabaseSchemaUpdate("true");

        List<BpmnParseHandler> customDefaultBpmnParseHandlers = new ArrayList<>();
        ExtensionUserTaskParseHandler extensionUserTaskParseHandler = new ExtensionUserTaskParseHandler();
        customDefaultBpmnParseHandlers.add(extensionUserTaskParseHandler);
        processEngineConfiguration.setCustomDefaultBpmnParseHandlers(customDefaultBpmnParseHandlers);

        List<ActivitiEventListener> customEventListeners =  new ArrayList<>();
        customEventListeners.add(new ProcessEventListener());
        processEngineConfiguration.setEventListeners(customEventListeners);
    }
}
