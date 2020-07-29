package com.bigdemo.demo.service;

import com.bigdemo.demo.entity.Address;
import com.bigdemo.demo.entity.vo.AddressAndEmployeeVO;
import com.bigdemo.demo.entity.vo.AddressVO;

import java.util.List;

public interface AddressService {

    List<AddressAndEmployeeVO> selectEmployeeByAddress(Address address);

    List<AddressVO> selectAddress();
}
