package com.example.activiti.entity;

import org.activiti.engine.impl.persistence.entity.TaskEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @Class CustomTaskEntity
 * @description: TODO
 * @Author thz
 * @Date 2019/7/22 15:54
 * @Version 1.0
 */
public class CustomTaskEntity {

    private String id;

    private String name;

    private String masterMen;

    private String generalMen;

    private boolean multiTask;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasterMen() {
        return masterMen;
    }

    public void setMasterMen(String masterMen) {
        this.masterMen = masterMen;
    }

    public String getGeneralMen() {
        return generalMen;
    }

    public void setGeneralMen(String generalMen) {
        this.generalMen = generalMen;
    }

    public boolean isMultiTask() {
        return multiTask;
    }

    public void setMultiTask(boolean multiTask) {
        this.multiTask = multiTask;
    }

    public List getAllField() {
        List<String> fieldList = new ArrayList<>();
        Field[] field = this.getClass().getFields();
        for (Field f : field) {
            fieldList.add(f.getName());
        }
        return fieldList;
    }
}
