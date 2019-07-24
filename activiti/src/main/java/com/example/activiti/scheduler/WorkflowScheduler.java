package com.example.activiti.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * @Class WorkflowScheduler
 * @description: TODO
 * @Author thz
 * @Date 2019/7/24 9:41
 * @Version 1.0
 */
@Component
public class WorkflowScheduler {

    /**
     * 1. 拿到所有的未被挂起的流程实例，循环
     * 2. 获取环节的办理时限
     * 3. 计算环节到期时间，存数据库
     */
}
