package com.sipc.catalina.controller;

import com.sipc.catalina.base.ResponseMessage;
import com.sipc.catalina.entity.Address;
import com.sipc.catalina.entity.Employee;
import com.sipc.catalina.repository.AddressRepository;
import com.sipc.catalina.repository.EmployeeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Api(tags = "地址")
@RestController
public class AddressController {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 所有地址
     *
     * @return
     */
    @GetMapping("/address")
    @ApiOperation("所有地址")
    public ResponseMessage findall() {
        List<Address> all = addressRepository.findAll();
        return ResponseMessage.success(all);
    }

    @ApiOperation("地址查员工")
    @GetMapping("/employees/{address}")
    public ResponseMessage findEmployesbyName(@PathVariable("address") String address) {
        Address address2 = new Address();
        address2.setAdName(address);
        Example<Address> example = Example.of(address2);
        List<Address> all = addressRepository.findAll(example);
        ArrayList<Employee> employees = new ArrayList<>();
        for (Address address1 : all) {
            Optional<Employee> byId = employeeRepository.findById(address1.getAdEmId());
            employees.add(byId.get());
        }
        return ResponseMessage.success(employees);
    }
}
