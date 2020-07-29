package com.bigdemo.demo.dao;

import com.bigdemo.demo.entity.Address;
import com.bigdemo.demo.entity.vo.AddressAndEmployeeVO;
import com.bigdemo.demo.entity.vo.AddressVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface AddressMapper {
    //根据地址查询员工
    List<AddressAndEmployeeVO> selectEmployeeByAddress(Address address);
    //查询地址表中的地址
    List<AddressVO> selectAddress();
}
