package com.bigdemo.demo.service.serviceImp;

import com.bigdemo.demo.dao.EmployeeMapper;

import com.bigdemo.demo.entity.Employee;
import com.bigdemo.demo.entity.vo.AddressAndEmployeeVO;
import com.bigdemo.demo.entity.vo.EmployeeVO;
import com.bigdemo.demo.service.EmployeeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class EmployeeServiceImp implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;


    public List<AddressAndEmployeeVO> selectAddressByEmployee(Employee employee) {
        return employeeMapper.selectAddressByEmployee(employee);
    }

    public List<EmployeeVO> selectEmployee() {
        return employeeMapper.selectEmployee();
    }
}
