package com.example.activiti.entity;

import java.util.ArrayList;
import java.util.List;

public enum ListenerTypeEnum {
    ACTIVITY_STARTED,
    ACTIVITY_COMPLETED,
    ENTITY_SUSPENDED;

    public static List<String> list() {
        List<String> list = new ArrayList<>();
        for (ListenerTypeEnum listenerTypeEnum : ListenerTypeEnum.values()) {
            list.add(listenerTypeEnum.toString());
        }
        return list;
    }
    }
