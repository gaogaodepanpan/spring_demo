package com.bigdemo.demo.dao;


import com.bigdemo.demo.entity.Employee;
import com.bigdemo.demo.entity.vo.AddressAndEmployeeVO;
import com.bigdemo.demo.entity.vo.EmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface EmployeeMapper {
    //根据员工找地址
    List<AddressAndEmployeeVO> selectAddressByEmployee(Employee employee);

    //查询员工表中员工
    List<EmployeeVO> selectEmployee();
}
