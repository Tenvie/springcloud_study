package com.example.activiti.service;

import com.example.activiti.entity.CustomTaskEntity;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Class ProcessDefinitionService
 * @description: 流程定义操作封装类
 * @Author thz
 * @Date 2019/7/24 10:30
 * @Version 1.0
 */
@Component
public class ProcessDefinitionService {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    private ActivityImpl getCurActivity(String processId) {
        HistoricTaskInstance curTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId).unfinished().singleResult();
        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(curTask.getProcessDefinitionId()));
        // 取得当前活动
        return definition.findActivity(curTask.getTaskDefinitionKey());
    }

    /**
     * 根据实例id获取当前任务
     *
     * @param processId
     * @return
     */
    public CustomTaskEntity getCurTaskByProcessId(String processId) {
        CustomTaskEntity customTaskEntity = new CustomTaskEntity();
        try {
            // 取得当前活动
            ActivityImpl currActivity = getCurActivity(processId);
            customTaskEntity = mapToBean(currActivity.getProperties(), CustomTaskEntity.class);
            customTaskEntity.setId(currActivity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customTaskEntity;
    }

    /**
     * 根据实例id获取下一步任务，包括退回过的任务
     *
     * @param processId
     * @return
     */
    public List<CustomTaskEntity> getNextTasks(String processId) {
        Map<String, CustomTaskEntity> taskMap = new HashMap<>();
        try {
            // 取得当前活动
            ActivityImpl currActivity = getCurActivity(processId);
            List<PvmTransition> pvmTransitions = currActivity.getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitions) {
                //获取所有的终点节点
                ActivityImpl nextTask = (ActivityImpl) pvmTransition.getDestination();
                CustomTaskEntity taskEntity = null;
                taskEntity = mapToBean(nextTask.getProperties(), CustomTaskEntity.class);
                taskEntity.setId(nextTask.getId());
                taskMap.put(taskEntity.getId(), taskEntity);
            }
            List<HistoricTaskInstance> backList = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId)
                    .taskDeleteReason("back")
                    .orderByHistoricTaskInstanceEndTime()
                    .desc()
                    .list();
            if (!backList.isEmpty()) {
                // 取得流程定义
                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(backList.get(0).getProcessDefinitionId()));
                // 取得当前活动
                ActivityImpl backActivity = definition.findActivity(backList.get(0).getTaskDefinitionKey());
                CustomTaskEntity taskEntity = mapToBean(backActivity.getProperties(), CustomTaskEntity.class);
                taskEntity.setId(backActivity.getId());
                taskMap.put(taskEntity.getId(), taskEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(taskMap.values());
    }

    /**
     * 根据实例id获取上一步任务
     *
     * @param processId
     * @return
     */
    public CustomTaskEntity getLastTask(String processId) {
        HistoricTaskInstance curTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId).unfinished().singleResult();
        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(curTask.getProcessDefinitionId()));
        // 取得当前活动
        ActivityImpl currActivity = definition.findActivity(curTask.getTaskDefinitionKey());
        //获取上一步活动
        List<PvmTransition> lastTransitionList = currActivity.getIncomingTransitions();
        PvmActivity lastActivity = lastTransitionList.get(0).getSource();
        ActivityImpl lastActivityImpl = definition.findActivity(lastActivity.getId());
        CustomTaskEntity taskEntity = null;
        try {
            taskEntity = mapToBean(lastActivityImpl.getProperties(), CustomTaskEntity.class);
            taskEntity.setId(lastActivityImpl.getId());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return taskEntity;
    }

    /**
     * 利用反射将map集合封装成bean对象
     *
     * @param map
     * @param clazz
     * @return
     */

    public static <T> T mapToBean(Map<String, Object> map, Class<?> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object obj = clazz.newInstance();
        if (map != null && !map.isEmpty() && map.size() > 0) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String propertyName = entry.getKey();    // 属性名
                Object value = entry.getValue();        // 属性值
                String setMethodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
                Field field = getClassField(clazz, propertyName);    //获取和map的key匹配的属性名称
                if (field == null) {
                    continue;
                }
                Class<?> fieldTypeClass = field.getType();
                value = convertValType(value, fieldTypeClass);
                try {
                    clazz.getMethod(setMethodName, field.getType()).invoke(obj, value);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        return (T) obj;
    }

    /**
     * 根据给定对象类匹配对象中的特定字段
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    private static Field getClassField(Class<?> clazz, String fieldName) {
        if (clazz.isAssignableFrom(Object.class)) {
            return null;
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        Class<?> superClass = clazz.getSuperclass();    //如果该类还有父类，将父类对象中的字段也取出
        if (superClass != null) {                        //递归获取
            return getClassField(superClass, fieldName);
        }
        return null;
    }

    /**
     * 将map的value值转为实体类中字段类型匹配的方法
     *
     * @param value
     * @param fieldTypeClass
     * @return
     */
    private static Object convertValType(Object value, Class<?> fieldTypeClass) {
        Object retVal = null;

        if (fieldTypeClass.isAssignableFrom(Long.class)
                || fieldTypeClass.isAssignableFrom(long.class)) {
            retVal = Long.parseLong(value.toString());
        } else if (fieldTypeClass.isAssignableFrom(Integer.class)
                || fieldTypeClass.isAssignableFrom(int.class)) {
            retVal = Integer.parseInt(value.toString());
        } else if (fieldTypeClass.isAssignableFrom(Float.class)
                || fieldTypeClass.isAssignableFrom(float.class)) {
            retVal = Float.parseFloat(value.toString());
        } else if (fieldTypeClass.isAssignableFrom(Double.class)
                || fieldTypeClass.isAssignableFrom(double.class)) {
            retVal = Double.parseDouble(value.toString());
        } else {
            retVal = value;
        }
        return retVal;
    }
}
