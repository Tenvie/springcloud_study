package com.example.activiti.entity;

import java.util.ArrayList;
import java.util.List;


/**
 * 扩展属性枚举类 ,type为0的需要创建处理起处理（复杂的处理）
 *
 * @author thz
 * @date
 */
public enum PropertyEnum {
    process(0),
    multiTask(0),
    masterMen(1),
    generalMen(1),
    timeLimit(1),
    accumulativeLimit(1);

    private int type;

    PropertyEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static List<String> list() {
        List list = new ArrayList();
        for (PropertyEnum propertyEnum : PropertyEnum.values()) {
            if (propertyEnum.type == 0) {
                list.add(propertyEnum.toString());
            }
        }
        return list;
    }

    public static List<String> all() {
        List list = new ArrayList();
        for (PropertyEnum propertyEnum : PropertyEnum.values()) {
                list.add(propertyEnum.toString());
        }
        return list;
    }
}
