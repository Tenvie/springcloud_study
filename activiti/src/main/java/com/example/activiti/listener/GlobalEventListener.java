package com.example.activiti.listener;

import com.example.activiti.entity.ListenerTypeEnum;
import com.example.activiti.utils.HandlerHelper;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.springframework.stereotype.Component;

/**
 * @Class ProcessEventListener
 * @description: 全局事件监听器
 * @Author thz
 * @Date 2019/7/18 14:19
 * @Version 1.0
 */
@Component
public class GlobalEventListener implements ActivitiEventListener {
    @Override
    public void onEvent(ActivitiEvent event) {
        /*ActivitiEventType eventType = event.getType();

        if(ActivitiEventType.ACTIVITY_STARTED.equals(eventType)){
            System.out.println("流程启动");
        }

        if(ActivitiEventType.ACTIVITY_COMPLETED.equals(eventType)){
            System.out.println("节点完成");
            List<HistoricActivityInstance> task= historyService.createHistoricActivityInstanceQuery().processInstanceId(event.getProcessInstanceId()).unfinished().list();
            ActivitiEntityEventImpl eventImpl=(ActivitiEntityEventImpl)event;
            CustomTaskEntity taskEntity=(CustomTaskEntity)eventImpl.getEntity();
        }*/
        if (ListenerTypeEnum.list().contains(event.getType().toString())) {
            HandlerHelper.handle(event.getType().toString(), event);
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
