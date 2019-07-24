package com.example.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * @Class GlobalTaskListener
 * @description: 全局节点监听器（create，assignment，complete，delete）
 * @Author thz
 * @Date 2019/7/23 10:22
 * @Version 1.0
 */
public class GlobalTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {

    }
}
