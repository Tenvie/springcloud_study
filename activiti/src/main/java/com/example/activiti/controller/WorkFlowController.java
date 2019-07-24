package com.example.activiti.controller;

import com.example.activiti.service.WorkflowTraceService;
import com.example.activiti.entity.CustomTaskEntity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class WorkFlowController {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    FormService formService;

    @Autowired
    IdentityService identityService;


    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    WorkflowTraceService traceService;

    @Autowired
    ProcessEngineFactoryBean processEngine;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;


    //获取所有流程定义
    @GetMapping(value = "/processDefinitions")
    public Map<String, String> findProcessDefinitions() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        return processDefinitions.stream().collect(Collectors.toMap(ProcessDefinition::getId, ProcessDefinition::getName));
    }

    /**
     * 输出跟踪流程信息
     *
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/processtrace")
    @ResponseBody
    public List<Map<String, Object>> traceProcess(@RequestParam("pid") String processInstanceId) throws Exception {
        List<Map<String, Object>> activityInfos = traceService.traceProcess(processInstanceId);
        return activityInfos;
    }

    /**
     * 读取带跟踪的图片
     */
    @RequestMapping(value = "/processtrace/auto/{pId}")
    public void readResource(@PathVariable("pId") String processInstanceId, HttpServletResponse response)
            throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        // 使用spring注入引擎请使用下面的这行代码
        processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds);

        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    /**
     * 读取资源，通过部署ID
     *
     * @param processDefinitionId 流程定义
     * @param resourceType        资源类型(xml|image)
     * @throws Exception
     */
    @RequestMapping(value = "/resource/read")
    public void loadByDeployment(@RequestParam("processDefinitionId") String processDefinitionId,
                                 @RequestParam("resourceType") String resourceType, HttpServletResponse response) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();

        String resourceName = "";
        if (resourceType.equals("image")) {
            resourceName = processDefinition.getDiagramResourceName();
        } else if (resourceType.equals("xml")) {
            resourceName = processDefinition.getResourceName();
        }
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                resourceName);
        byte[] b = new byte[1024];
        int len = -1;
        try {
            while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //启动一个流程
    @GetMapping(value = "/start")
    public String start(String processDefinitionId) {
        return runtimeService.startProcessInstanceById(processDefinitionId).getId();
    }

    //查询流程实例下一步的任务
    @GetMapping(value = "/getNextTasks")
    @ResponseBody
    public List<CustomTaskEntity> getNextTasks(String processId) {
        List<CustomTaskEntity> result = new ArrayList<>();
        // 取得当前任务
        List<HistoricTaskInstance> currTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId).list();
        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(currTask.get(0).getProcessDefinitionId()));
        // 取得当前活动
        ActivityImpl currActivity = definition.findActivity(currTask.get(0).getTaskDefinitionKey());
        List<PvmTransition> pvmTransitions = currActivity.getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitions) {
            ActivityImpl nextTask = (ActivityImpl) pvmTransition.getDestination();//获取所有的终点节点
            CustomTaskEntity taskEntity = new CustomTaskEntity();
            taskEntity.setMasterMen(nextTask.getProperty("masterMen").toString());
            taskEntity.setId(nextTask.getId());
            taskEntity.setName(nextTask.getProperty("name").toString());
            result.add(taskEntity);
        }
        return result;
    }

    //完成一个任务
    @GetMapping(value = "/finish")
    public void finish(String taskId) {
        taskService.complete(taskId);
    }

    //转办或者接办任务
    @GetMapping(value = "/setAssigneeTask")
    public void trunTask(String taskId, String assignee) {
        taskService.setAssignee(taskId, assignee);
    }

    //挂起流程实例
    @GetMapping(value = "/suspendProcessInstance")
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    //解除挂起（激活）流程实例
    @GetMapping(value = "/activateProcessInstanceById")
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    //回退任务
    @GetMapping(value = "/runtime/backProcess")
    public void backProcess(String taskId) {
        try {
            // 取得当前任务
            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            // 取得流程实例
            ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(currTask.getProcessDefinitionId()));
            if (definition == null) {
                return;
            }
            // 取得当前活动
            ActivityImpl currActivity = definition.findActivity(currTask.getTaskDefinitionKey());
            //获取上一步活动
            List<PvmTransition> lastTransitionList = currActivity.getIncomingTransitions();
            // 清除当前活动的出口
            List<PvmTransition> oriPvmTransitionList = new ArrayList<>();
            List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitionList) {
                oriPvmTransitionList.add(pvmTransition);
            }
            pvmTransitionList.clear();

            // 建立新出口
            List<TransitionImpl> newTransitions = new ArrayList<>();
            for (PvmTransition nextTransition : lastTransitionList) {
                PvmActivity nextActivity = nextTransition.getSource();
                ActivityImpl nextActivityImpl = definition.findActivity(nextActivity.getId());
                TransitionImpl newTransition = currActivity.createOutgoingTransition();
                newTransition.setDestination(nextActivityImpl);
                newTransitions.add(newTransition);
            }
            // 完成任务
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
            for (Task task : tasks) {
                taskService.complete(task.getId());
                historyService.deleteHistoricTaskInstance(task.getId());
            }
            // 恢复方向
            for (TransitionImpl transitionImpl : newTransitions) {
                currActivity.getOutgoingTransitions().remove(transitionImpl);
            }
            for (PvmTransition pvmTransition : oriPvmTransitionList) {
                pvmTransitionList.add(pvmTransition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/activiti/multiTask")
    public void multiTask() {
        // 流程部署
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("process/MultiTask.bpmn")
                .name("流程测试")
                .category("")
                .deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        System.out.println("流程名称 ： [" + processDefinition.getName() + "]， 流程ID ： ["
                + processDefinition.getId() + "], 流程KEY : " + processDefinition.getKey());
        // 启动流程
        // 分配任务的人员
        List<String> assigneeList = new ArrayList<String>();
        assigneeList.add("tom");
        assigneeList.add("jeck");
        assigneeList.add("mary");
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("assigneeList", assigneeList);
        //identityService.setAuthenticatedUserId("createUserId");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", "Key001", vars);

        System.out.println("流程实例ID = " + processInstance.getId());
        System.out.println("正在活动的流程节点ID = " + processInstance.getActivityId());
        System.out.println("流程定义ID = " + processInstance.getProcessDefinitionId());

        // 查询指定人的任务
        // ============ 会签任务开始 ===========
       /* Map map = new HashMap();
        map.put("audit","yes");*/
        List<Task> taskList1 = taskService.createTaskQuery().taskAssignee("mary").orderByTaskCreateTime().desc().list();
        System.out.println("taskList1 = " + taskList1);
        Task task1 = taskList1.get(0);
        taskService.complete(task1.getId());

        List<Task> taskList2 = taskService.createTaskQuery().taskAssignee("jeck").orderByTaskCreateTime().desc().list();
        System.out.println("taskList2 = " + taskList2);
        Task task2 = taskList2.get(0);
        taskService.complete(task2.getId());

        List<Task> taskList3 = taskService.createTaskQuery().taskAssignee("tom").orderByTaskCreateTime().desc().list();
        System.out.println("taskList3 = " + taskList3);

        Task task3 = taskList3.get(0);
        taskService.complete(task3.getId());
        // ============ 会签任务结束 ===========
    }


}