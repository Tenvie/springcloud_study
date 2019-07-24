package com.example.activiti.dao;

import com.example.activiti.entity.CustomInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Interface CustomInstanceDao
 * @description: TODO
 * @Author thz
 * @Date 2019/7/24 14:17
 * @Version 1.0
 */
@Repository(CustomInstanceDao.BEAN_NAME)
public interface CustomInstanceDao extends JpaRepository<CustomInstanceEntity, String> {
    /**
     * Spring Bean名称。
     */
    public static final String BEAN_NAME = "customInstanceDao";
}
