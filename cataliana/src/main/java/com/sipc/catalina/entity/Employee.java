package com.sipc.catalina.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="em_id")
    private Integer emId; //员工号
    @Column(name="em_name")
    private String emName; //姓名
    public Employee(){

    }
//    @OneToMany(targetEntity = Address.class)
//    @JoinColumn(name="ad_em_id",referencedColumnName = "em_id")
    //放弃外键维护权
    @OneToMany(mappedBy = "employee")
    private Set<Address> address = new HashSet<>();


    public Integer getEmId() {
        return emId;
    }

    public Set<Address> getAddress() {
        return address;
    }

    public void setAddress(Set<Address> address) {
        this.address = address;
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

    @Override
    public String toString() {
        return "Employee{" +
                "emId=" + emId +
                ", emName='" + emName + '\'' +
                '}';
    }
}
