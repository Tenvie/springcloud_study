package com.example.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * @Class GlobalExecutionListener
 * @description: 全局监听器（流程实例start、end、take的时候调用。take是监控连线的时候使用的。）
 * @Author thz
 * @Date 2019/7/23 10:18
 * @Version 1.0
 */
public class GlobalExecutionListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {

    }
}
