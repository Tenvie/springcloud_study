package com.example.activiti.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.activiti.engine.task.Task;

import java.util.List;

/**
 * @Class ActiviUtils
 * @description: TODO
 * @Author thz
 * @Date 2019/7/12
 * @Version 1.0
 */
public class ActiviUtils {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runservice;

    @Autowired
    FormService formservice;

    @Autowired
    IdentityService identityservice;


    @Autowired
    TaskService taskservice;

    @Autowired
    HistoryService historyservice;

    /**
     * 设置会签节点属性 会签相关变量注释：nrOfInstances：实例总数 nrOfActiveInstances：当前活动的，比如，还没完成的，实例数量。 对于顺序执行的多实例，值一直为1 nrOfCompletedInstances：已经完成实例的数目
     * 可以通过execution.getVariable(x)方法获得这些变量
     *
     * @param modelId
     *            模型id
     * @param nodelId
     *            流程对象id
     */
    public void setMultiInstance(String modelId, String nodelId) throws Exception {
        // 获取模型
        byte[] mes = repositoryService.getModelEditorSource(modelId);
        // 转换成JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mes);
        // 转换成BpmnModel
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(jsonNode);
        // 获取物理形态的流程
        org.activiti.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
        // 获取节点信息
        FlowElement flowElement = process.getFlowElement(nodelId);
        // 只有人工任务才可以设置会签节点
        UserTask userTask = (UserTask) flowElement;
        // 设置受理人，这里应该和ElementVariable的值是相同的
        userTask.setOwner("${user}");

        // 获取多实例配置
        MultiInstanceLoopCharacteristics characteristics = new MultiInstanceLoopCharacteristics();
        // 设置集合变量，统一设置成users
        characteristics.setInputDataItem("users");
        // 设置变量
        characteristics.setElementVariable( "user");
        // 设置为同时接收（false 表示不按顺序执行）
        characteristics.setSequential(false);
        // 设置条件（暂时处理成，全部会签完转下步）
        characteristics.setCompletionCondition("${nrOfCompletedInstances==nrOfInstances}");

        userTask.setLoopCharacteristics(characteristics);
        // 保存
        ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        repositoryService.addModelEditorSource(modelId, objectNode.toString().getBytes("utf-8"));
    }

    /**
     * 清空会签属性
     *
     * @param modelId
     *            模型id
     * @param nodelId
     *            流程对象id
     * @throws Exception
     */
    public void clearMultiInstance(String modelId, String nodelId) throws Exception {
        // 获取模型
        byte[] mes = repositoryService.getModelEditorSource(modelId);
        // 转换成JsonNode
        JsonNode jsonNode = new ObjectMapper().readTree(mes);
        // 转换成BpmnModel
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(jsonNode);
        // 获取物理形态的流程
        org.activiti.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
        // 获取节点信息
        FlowElement flowElement = process.getFlowElement(nodelId);
        // 只有人工任务才可以设置会签节点
        UserTask userTask = (UserTask) flowElement;
        // 清空受理人
        userTask.setAssignee("");
        // 获取多实例配置
        MultiInstanceLoopCharacteristics characteristics = userTask.getLoopCharacteristics();
        if (characteristics != null) {
            // 清空集合
            characteristics.setInputDataItem("");
            // 清空变量
            characteristics.setElementVariable("");
            // 设置为顺序接收（true 表示不按顺序执行）
            characteristics.setSequential(true);
            // 清空条件
            characteristics.setCompletionCondition("");
        }

        // 保存
        ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        repositoryService.addModelEditorSource(modelId, objectNode.toString().getBytes("utf-8"));
    }

    /**
     * 增加流程连线条件
     *
     * @param modelId
     *            模型id
     * @param nodelId
     *            流程对象id
     * @param condition
     *            el 条件表达式
     */
    public  void setSequenceFlowCondition(String modelId, String nodelId, String condition) throws Exception {
        // 获取模型--设置连线条件 到 流程中
        byte[] bytes = repositoryService.getModelEditorSource(modelId);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(bytes);
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);
        FlowElement flowElement = bpmnModel.getFlowElement(nodelId);
        if (!(flowElement instanceof SequenceFlow)) {
            throw new Exception("不是连线，不能设置条件");
        }
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
        sequenceFlow.setConditionExpression(condition);
        ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        repositoryService.addModelEditorSource(modelId, objectNode.toString().getBytes("utf-8"));
    }

    /************************* 回退开始 ***************************/

    /**
     * 根据任务ID获取对应的流程实例
     *
     * @param taskId
     *            任务ID
     * @return
     * @throws Exception
     */
    public ProcessInstance findProcessInstanceByTaskId(String taskId) throws Exception {
        // 找到流程实例
        ProcessInstance processInstance = runservice.createProcessInstanceQuery().processInstanceId(findTaskById(taskId).getProcessInstanceId())
                .singleResult();
        if (processInstance == null) {
            throw new Exception("流程实例未找到!");
        }
        return processInstance;
    }

    /**
     * 根据任务ID获得任务实例
     *
     * @param taskId
     *            任务ID
     * @return TaskEntity
     * @throws Exception
     */
    private TaskEntity findTaskById(String taskId) throws Exception {
        TaskEntity task = (TaskEntity) taskservice.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new Exception("任务实例未找到!");
        }
        return task;
    }

    /**
     * 设置任务处理人,根据任务ID
     *
     * @param taskId
     * @param userCode
     */
    public  void setTaskDealerByTaskId(String taskId, String userCode) {
        taskservice.setAssignee(taskId, userCode);
    }

    /**
     * 根据流程对象Id,查询当前节点Id
     *
     * @param executionId
     * @return
     */
    public  String getActiviIdByExecutionId(String executionId) {
        // 根据任务获取当前流程执行ID，执行实例以及当前流程节点的ID：
        ExecutionEntity execution = (ExecutionEntity) runservice.createExecutionQuery().executionId(executionId).singleResult();
        String activitiId = execution.getActivityId();
        return activitiId;
    }

    /**
     * 根据流程实例ID和任务key值查询所有同级任务集合
     *
     * @param processInstanceId
     * @param key
     * @return
     */
    public  List<Task> findTaskListByKey(String processInstanceId, String key) {
        List<Task> list = taskservice.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(key).list();
        return list;
    }

    /**
     * 根据任务ID获取流程定义
     *
     * @param taskId
     *            任务ID
     * @return
     * @throws Exception
     */
    public ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId) throws Exception {
        // 取得流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(findTaskById(taskId).getProcessDefinitionId());

        if (processDefinition == null) {
            throw new Exception("流程定义未找到!");
        }

        return processDefinition;
    }

    /**
     * 根据任务Id,查找当前任务
     *
     * @param taskId
     *            任务Id
     * @return
     */
    public  Task getTaskById(String taskId) {
        // 当前处理人的任务
        Task currenTask = taskservice.createTaskQuery().taskId(taskId).singleResult();
        return currenTask;
    }

    /**
     * 根据流程实例查询正在执行的任务
     *
     * @param processInst
     * @return
     */
    public  List<Task> getTaskByProcessInst(String processInst) {
        List<Task> list = taskservice.createTaskQuery().processInstanceId(processInst).list();
        return list;
    }

    /*
     *  顺序会签后退时处理
     *
     * @param instanceId 流程实例
     *
     * @param comment 意见
     *
     * @param preActivityId 上个已经完成任务ID
     */
    public  void dealMultiSequential(String instanceId, String comment, String preActivityId) {
        // 该流程实例正在运行的任务
        List<Task> runTask = getTaskByProcessInst(instanceId);
        for (Task t : runTask) {
            String runActivityId = t.getTaskDefinitionKey();
            // 正在运行的任务节点id和上个已经完成的任务节点id相等,则判定为顺序会签
            if (runActivityId.equals(preActivityId)) {
                if (comment == null) {
                    comment = "";
                }
                taskservice.addComment(t.getId(), t.getProcessInstanceId(), comment);
                // 执行转向任务
                t.setDescription("callback");
                taskservice.saveTask(t);
                taskservice.complete(t.getId());
                // 递归顺序会签,直到正在运行的任务还是顺序会签
                dealMultiSequential(t.getProcessInstanceId(), comment, t.getTaskDefinitionKey());
            }
        }
    }

    /**
     * 设置回退的任务处理人
     *
     * @param task
     *            当前任务
     * @param activityId
     *            回退节点ID
     */
    public  void setBackTaskDealer(Task task, String activityId) {
        List<HistoricTaskInstance> list = historyservice.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId())
                .taskDefinitionKey(activityId).taskDeleteReason("completed").orderByTaskCreateTime().desc().list();
        HistoricTaskInstance historicTask = null;
        if (list != null && list.size() > 0) {
            historicTask = list.get(0);
            // 查询回退后的节点正在运行的任务
            List<Task> taskList = taskservice.createTaskQuery().processInstanceId(task.getProcessInstanceId()).taskDefinitionKey(activityId).active().list();
            // 同一节点下有多个任务，则认定为会签任务
            if (taskList != null && taskList.size() > 1) {
                for (int i = 0; i < taskList.size(); i++) {
                    // 设置会签任务处理人（处理人顺序不管）
                    taskservice.setAssignee(taskList.get(i).getId(), list.get(i).getAssignee());
                }
            } else {
                Task taskNew = taskList.get(0);
                // 顺序会签流程变量处理人
                String variable = (String) runservice.getVariable(taskNew.getExecutionId(), "countersign");
                if (!StringUtils.isEmpty(variable)) {
                    // 设置下个顺序会签处理人
                    setTaskDealerByTaskId(taskNew.getId(), variable);
                } else {
                    // 设置一般回退任务处理人
                    taskservice.setAssignee(taskNew.getId(), historicTask.getAssignee());
                }
            }
        }
    }

    /**
     * 转办流程
     *
     * @param taskId
     *            当前任务节点ID
     * @param userId
     *            被转办人id
     */
    public  boolean transferAssignee(String taskId, String userId) {
        try {
            taskservice.setAssignee(taskId, userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
