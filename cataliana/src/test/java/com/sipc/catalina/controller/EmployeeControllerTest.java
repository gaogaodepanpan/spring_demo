package com.sipc.catalina.controller;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
class EmployeeControllerTest {

    @Autowired
    private EmployeeController controller;

    @Test
    void getAll() {
        Assert.assertNotNull(controller.getAll());
        System.out.println("通过测试！");


    }

    @Test
    void findById() {
        Assert.assertNotEquals(null, controller.findById(10));
    }


}