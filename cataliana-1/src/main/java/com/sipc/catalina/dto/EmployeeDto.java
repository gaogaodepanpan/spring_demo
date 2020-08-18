package com.sipc.catalina.dto;

import com.sipc.catalina.entity.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: describe:
 * time: 2020/8/15
 **/
public class EmployeeDto {

    private Integer emId;

    private String emName;

    private String age;

    public Integer getEmId() {
        return emId;
    }

    public void setEmId(Integer emId) {
        this.emId = emId;
    }

    public String getEmName() {
        return emName;
    }

    public void setEmName(String emName) {
        this.emName = emName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
