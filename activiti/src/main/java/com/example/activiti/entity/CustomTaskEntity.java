package com.example.activiti.entity;

import lombok.Data;

/**
 * @Class CustomTaskEntity
 * @description: TODO
 * @Author thz
 * @Date 2019/7/24 10:44
 * @Version 1.0
 */
@Data
public class CustomTaskEntity {

    private String id;

    //实例名称
    private String name;

    //主要处理人
    private String masterMen;

    //一般处理人
    private String generalMen;

    //是否是会签任务
    private String multiTask;

    //办理时限
    private double timeLimit;

    //累计办理时限
    private double accumulativeLimit;
}
