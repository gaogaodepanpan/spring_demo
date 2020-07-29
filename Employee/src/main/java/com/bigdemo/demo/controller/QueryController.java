package com.bigdemo.demo.controller;

import com.bigdemo.demo.Message;
import com.bigdemo.demo.entity.Address;
import com.bigdemo.demo.entity.Employee;
import com.bigdemo.demo.entity.vo.AddressAndEmployeeVO;
import com.bigdemo.demo.entity.vo.AddressVO;
import com.bigdemo.demo.entity.vo.EmployeeVO;
import com.bigdemo.demo.service.AddressService;
import com.bigdemo.demo.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/get") //
@Slf4j
public class QueryController {

    @Resource
    private AddressService addressService;
    @Resource
    private EmployeeService employeeService;

    @RequestMapping("/selectEmployeeByAddress")
    public Message selectAddress(@RequestBody Address address) {
        try {
            List<AddressAndEmployeeVO> addressAndEmployeeVOS = addressService.selectEmployeeByAddress(address);
            return Message.success(addressAndEmployeeVOS);
        } catch (Exception e) {
            return Message.exception(e.getMessage());
        }
    }

    @RequestMapping("/selectAddressByEmploy")
    public Message selectEmploy(@RequestBody Employee employee) {
        try {
            List<AddressAndEmployeeVO> employeeVOS = employeeService.selectAddressByEmployee(employee);
            return Message.success(employeeVOS);
        } catch (Exception e) {
            return Message.exception(e.getMessage());
        }
    }

    @RequestMapping("/selectEmploy")
    public Message selectEmploy1() {
        try {
            List<EmployeeVO> employees = employeeService.selectEmployee();
            return Message.success(employees);
        } catch (Exception e) {
            return Message.exception(e.getMessage());
        }
    }

    @RequestMapping("/selectAddress")
    public Message selectAddress1() {
        try {
            List<AddressVO> addresses = addressService.selectAddress();
            return Message.success(addresses);
        } catch (Exception e) {
            return Message.exception(e.getMessage());
        }
    }
}
