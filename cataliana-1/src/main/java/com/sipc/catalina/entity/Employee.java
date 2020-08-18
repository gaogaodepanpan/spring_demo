package com.sipc.catalina.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue
    @Column(name="em_id")
    private Integer emId; //员工号
    @Column(name="em_name")
    private String emName; //姓名
    @Column(name="age")
    private Integer age;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public Integer getEmId() {
        return emId;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "emId=" + emId +
                ", emName='" + emName + '\'' +
                '}';
    }


}
