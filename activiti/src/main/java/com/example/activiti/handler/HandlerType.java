package com.example.activiti.handler;

import java.lang.annotation.*;

/**
 * @Interface ExtraProperty
 * @Author thz
 * @Date 2019/7/18 14:57
 * @Version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HandlerType {
    String value();
}
