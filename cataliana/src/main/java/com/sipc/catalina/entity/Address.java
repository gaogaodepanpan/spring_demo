package com.sipc.catalina.entity;

import javax.persistence.*;

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private Long adId; //联系人编号(主键)
    @Column(name = "ad_name")
    private String adName;//联系人姓名
    public Address(){

    }

    /**
     * 配置联系人到客户的多对一关系
     * 使用注解的形式配置多对一关系
     * 1.配置表关系
     *
     * @ManyToOne : 配置多对一关系
     * targetEntity：对方的实体类字节码
     * 2.配置外键（中间表）
     * <p>
     * * 配置外键的过程，配置到了多的一方，就会在多的一方维护外键
     */

    @ManyToOne(targetEntity = Employee.class, fetch = FetchType.LAZY) //懒加载
    @JoinColumn(name = "ad_em_id", referencedColumnName = "em_id")
    private Employee employee;


    public Long getAdId() {
        return adId;
    }

    public void setAdId(Long adId) {
        this.adId = adId;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}