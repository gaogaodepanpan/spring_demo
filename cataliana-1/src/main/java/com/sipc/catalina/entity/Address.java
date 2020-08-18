package com.sipc.catalina.entity;

import javax.persistence.*;

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue
    @Column(name = "ad_id")
    private Long adId; //联系人编号(主键)
    @Column(name = "ad_name")
    private String adName;//联系人姓名
    @Column(name = "ad_em_id")
    private Integer adEmId;

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

    public Integer getAdEmId() {
        return adEmId;
    }

    public void setAdEmId(Integer adEmId) {
        this.adEmId = adEmId;
    }
}