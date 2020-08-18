package com.sipc.catalina.controller;

import com.google.common.collect.Lists;
import com.sipc.catalina.base.ResponseMessage;
import com.sipc.catalina.dto.EmployeeDto;
import com.sipc.catalina.entity.Address;
import com.sipc.catalina.entity.Employee;
import com.sipc.catalina.enums.ExamErrorCodeEnum;
import com.sipc.catalina.repository.AddressRepository;
import com.sipc.catalina.repository.EmployeeRepository;
import com.sipc.catalina.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "员工")
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private EmployeeService employeeService;

    /**
     * 1.获取学生所有信息
     */
    @ApiOperation("所有员工")
    @GetMapping("/employees")
    public ResponseMessage getAll() {
        List<Employee> all = employeeRepository.findAll();
        return ResponseMessage.success(all);
    }

    @ApiOperation("所有员工年龄")
    @GetMapping("/employees/age")
    public ResponseMessage getAllage() {
        List<EmployeeDto> all = employeeService.findAllAge();
        return ResponseMessage.success(all);
    }

    @ApiOperation("根据员工Id查询年龄")
    @GetMapping("/employees/age/{eId}")
    public ResponseMessage findAgeByEid(@PathVariable("eId") Integer eId) {
        List<Integer> ids = Lists.newArrayList();
        if(eId == null){
            return ResponseMessage.error(ExamErrorCodeEnum.PARAM_NULL.getCode().toString(),"参数为空");
        }
        ids.add(eId);
        List<EmployeeDto> all = employeeService.findAge(ids);

        return ResponseMessage.success(all);
    }






    /**
     * 2.通过员工id查找他的地址
     *
     * @return
     */
    @ApiOperation("通过员工id查找他的地址")
    @GetMapping("/employees/address/{eId}")
    public ResponseMessage findAddressByEid(@PathVariable("eId") Integer eId) {
        Address address = new Address();
        address.setAdEmId(eId);
        Example<Address> example = Example.of(address);
        List<Address> all = addressRepository.findAll(example);
        return ResponseMessage.success(all);
    }

}
