package com.example.activiti.controller;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workflow")
public class WorkFlow {

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


    //获取所有流程定义
    @GetMapping(value = "/repository/processDefinitions")
    public Map<String, String> findProcessDefinitions() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        return processDefinitions.stream().collect(Collectors.toMap(ProcessDefinition::getId, ProcessDefinition::getName));
    }

    //启动一个流程
    @GetMapping(value = "/runtime/start")
    public String start(String processDefinitionId) {
        return runtimeService.startProcessInstanceById(processDefinitionId).getId();
    }

    //查询流程实例下一步的任务
    @GetMapping(value = "/runtime/getNextTasks")
    public Map getNextTasks(String processId) {
       List<HistoricActivityInstance> task= historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).unfinished().list();
       Map<String,String> result = new HashMap<>();
       task.forEach(historicActivityInstance ->result.put(historicActivityInstance.getTaskId(),historicActivityInstance.getActivityName()));
       return result;
    }

    //完成一个任务
    @GetMapping(value = "/runtime/finish")
    public void finish(String taskId,String variable){
       taskService.complete(taskId,Collections.singletonMap("type",variable));
    }

    //完成任务，带流程变量

    //设置一个任务节点为会签
    @GetMapping(value = "/task/setMultiInstance")
    public void setMultiInstance(String taskId,String nodelId){
        ActiviUtils activiUtils = new ActiviUtils();
        try {
            Task task=taskService.createTaskQuery() // 创建任务查询
                    .taskId(taskId) // 根据任务id查询
                    .singleResult();
            String processDefinitionId=task.getProcessDefinitionId(); // 获取流程定义id
            ProcessDefinition processDefinition=repositoryService.createProcessDefinitionQuery() // 创建流程定义查询
                    .processDefinitionId(processDefinitionId) // 根据流程定义id查询
                    .singleResult();
            String modelId =repositoryService.createModelQuery().deploymentId(processDefinition.getDeploymentId()).singleResult().getId();
            activiUtils.setMultiInstance(modelId,nodelId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //回退任务
    @GetMapping(value = "/runtime/backProcess")
    public void backProcess(String taskId){
        try {
            // 取得当前任务
            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            // 取得流程实例
            ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(currTask.getProcessDefinitionId()));
            if (definition == null) {
                return ;
            }
            // 取得当前活动
            ActivityImpl currActivity =  definition.findActivity(currTask.getTaskDefinitionKey());
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
        identityService.setAuthenticatedUserId("createUserId");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", "Key001", vars);

        System.out.println("流程实例ID = " + processInstance.getId());
        System.out.println("正在活动的流程节点ID = " + processInstance.getActivityId());
        System.out.println("流程定义ID = " + processInstance.getProcessDefinitionId());

        // 查询指定人的任务
        // ============ 会签任务开始 ===========
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
