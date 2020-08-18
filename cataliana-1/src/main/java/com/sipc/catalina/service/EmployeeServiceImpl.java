package com.sipc.catalina.service;

import com.google.common.collect.Lists;
import com.sipc.catalina.dto.EmployeeDto;
import com.sipc.catalina.entity.Employee;
import com.sipc.catalina.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: describe:
 * time: 2020/8/15
 **/
@Service
public class EmployeeServiceImpl implements EmployeeService{
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public List<EmployeeDto> findAllAge() {
        List<Employee> all = employeeRepository.findAll();

        return replaceAge(all);
    }

    @Override
    public List<EmployeeDto> findAge(List<Integer> ids) {
        List<Employee> all = employeeRepository.findAllById(ids);
        return replaceAge(all);
    }

    protected List<EmployeeDto> replaceAge(List<Employee> employeeList){
        ArrayList<EmployeeDto> employeeDtos = Lists.newArrayList();
        for (Employee employee : employeeList) {
            EmployeeDto employeeDto = new EmployeeDto();
            employeeDto.setEmId(employee.getEmId());
            employeeDto.setEmName(employee.getEmName());
            if (employee.getAge()<18){
                employeeDto.setAge("少年");
            }
            if (employee.getAge()>=18&&employee.getAge()<30){
                employeeDto.setAge("青年");
            }
            if(employee.getAge()>=30&&employee.getAge()<50){
                employeeDto.setAge("中年");
            }
            if (employee.getAge()>=50){
                employeeDto.setAge("老年");
            }
            employeeDtos.add(employeeDto);
        }
        return employeeDtos;
    }
}
