package com.bigdemo.demo.service;

import com.bigdemo.demo.entity.Employee;
import com.bigdemo.demo.entity.vo.AddressAndEmployeeVO;
import com.bigdemo.demo.entity.vo.EmployeeVO;

import java.util.List;

public interface EmployeeService {
    //根据员工找地址
    List<AddressAndEmployeeVO> selectAddressByEmployee(Employee employee);

    List<EmployeeVO> selectEmployee();
}
