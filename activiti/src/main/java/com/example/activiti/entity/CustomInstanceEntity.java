package com.example.activiti.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Class CustomTaskEntity
 * @description: TODO
 * @Author thz
 * @Date 2019/7/22 15:54
 * @Version 1.0
 */
@Data
@Entity
@Table(name = "custominstance")
public class CustomInstanceEntity {

    @Id
    @Column(name = "processid")
    private String processId;

    //实例名称
    @Column(name = "name")
    private String name;

    //环节实例到期时间.
    @Transient
    private Date duDate;

    //流程实例剩余时间（单位工作日）.
    @Transient
    private double surplusDate;

    //流程最大时限.
    @Transient
    private double maxTimelimit;

    // 流程实例启动时间.
    @Column(name = "startdatetime")
    private Date startDateTime;

    // 流程实例启动者Id.
    @Column(name = "startuser")
    private String startUser;

    // 流程实例启动者名称.
    @Column(name = "startusername")
    private String startUserName;

    //流程管理者.
    @Column(name = "managers")
    private String managers;

    //关联表单id.
    @Column(name = "formid")
    private String formId;

    //正在办理的任务环节id
    @Column(name = "taskid")
    private String taskId;

    //正在办理的任务环节名称
    @Column(name = "taskname")
    private String taskName;
}
