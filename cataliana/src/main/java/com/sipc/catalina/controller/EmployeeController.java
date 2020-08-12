package com.sipc.catalina.controller;

import com.sipc.catalina.entity.Employee;
import com.sipc.catalina.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class EmployeeController {

    @Autowired
    private EmployeeRepository repository;

    /**
     * 1.获取学生所有信息
     */
    @GetMapping("/employees")
    public List<Employee> getAll() {
        return repository.findAll();
    }

    /**
     * 2.通过id查找一个员工
     *
     * @return
     */
    @GetMapping("/students/{id}")
    public Object findById(@PathVariable("id") Integer id) {
        return repository.findById(id).orElse(null);
    }

}
