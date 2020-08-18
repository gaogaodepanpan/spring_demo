package com.sipc.catalina.service;

import com.sipc.catalina.dto.EmployeeDto;
import com.sipc.catalina.entity.Employee;

import java.util.List;

/**
 * @author: describe:
 * time: 2020/8/15
 **/
public interface EmployeeService {
    List<EmployeeDto> findAllAge();
    List<EmployeeDto> findAge(List<Integer> ids);
}
