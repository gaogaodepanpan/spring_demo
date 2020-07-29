package com.bigdemo.demo.entity;

import lombok.Data;


//@Data
@Data
//@Entity
//@Table(name = "address")
//@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class Address {
//    @Id
//    @GeneratedValue(generator = "jpa-uuid")
//    @Column(name = "base_id", updatable = false)
//    private String baseId;

    private String addressId;

    private String employeeId;

    private String addressName;
}
