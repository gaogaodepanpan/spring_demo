package com.bigdemo.demo.service.serviceImp;

import com.bigdemo.demo.dao.AddressMapper;
import com.bigdemo.demo.entity.Address;
import com.bigdemo.demo.entity.vo.AddressAndEmployeeVO;
import com.bigdemo.demo.entity.vo.AddressVO;
import com.bigdemo.demo.service.AddressService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
//service表示这个类是服务层用来写复杂逻辑的！这个注解写在类名上，auto是引用别的类，简单理解就是把别的类new过来了，但是这个类创建一次。
//@Service    -- service标注业务层组件
//这个注解是写在类上面的，标注将这个类交给Spring容器管理，spring容器要为他创建对象
@Service
public class AddressServiceImp implements AddressService {
    @Resource
    private AddressMapper addressMapper;


    public List<AddressAndEmployeeVO> selectEmployeeByAddress(Address address) {
        return addressMapper.selectEmployeeByAddress(address);
    }

    public List<AddressVO> selectAddress() {
        return addressMapper.selectAddress();
    }
}
