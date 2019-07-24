package com.example.activiti.service;

import com.example.activiti.entity.CustomTaskEntity;
import com.example.activiti.entity.PropertyEnum;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Class ProcessDefinitionService
 * @description: TODO
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

    //根据实例id获取当前任务
    public CustomTaskEntity getCurTaskByProcessId(String processId) {
        CustomTaskEntity customTaskEntity = new CustomTaskEntity();
        try {
            HistoricTaskInstance curTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId).unfinished().singleResult();
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) (repositoryService.getProcessDefinition(curTask.getProcessDefinitionId()));
            // 取得当前活动
            ActivityImpl currActivity = definition.findActivity(curTask.getTaskDefinitionKey());
            customTaskEntity = mapToBean(currActivity.getProperties(), CustomTaskEntity.class);
            customTaskEntity.setId(curTask.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customTaskEntity;
    }

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
            CustomTaskEntity taskEntity = null;
            try {
                taskEntity = mapToBean(nextTask.getProperties(), CustomTaskEntity.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            result.add(taskEntity);
        }
        return result;
    }

    /**
     * 利用反射将map集合封装成bean对象
     *
     * @param map
     * @param clazz
     * @return
     */

    public static <T> T mapToBean(Map<String, Object> map, Class<?> clazz) throws Exception {
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
        if (Object.class.getName().equals(clazz.getName())) {
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

        if (Long.class.getName().equals(fieldTypeClass.getName())
                || long.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Long.parseLong(value.toString());
        } else if (Integer.class.getName().equals(fieldTypeClass.getName())
                || int.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Integer.parseInt(value.toString());
        } else if (Float.class.getName().equals(fieldTypeClass.getName())
                || float.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Float.parseFloat(value.toString());
        } else if (Double.class.getName().equals(fieldTypeClass.getName())
                || double.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Double.parseDouble(value.toString());
        } else {
            retVal = value;
        }
        return retVal;
    }
}
