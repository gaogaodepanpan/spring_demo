package com.sipc.catalina.service;

import com.sipc.catalina.dto.EmployeeDto;
import com.sipc.catalina.entity.Employee;
import com.sipc.catalina.repository.EmployeeRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest(EmployeeServiceImpl.class)
public class EmployeeServiceImplTest {

    @Test
    public void testFindAllAge() throws Exception {
        //mock数据访问
      EmployeeRepository emr = PowerMockito.mock(EmployeeRepository.class);
      Employee emt = new Employee();
      emt.setAge(10);
      emt.setEmId(10);
      emt.setEmName("测试");
      PowerMockito.when(emr.findAll()).thenReturn((List<Employee>) emt);
      //测试方法
      EmployeeServiceImpl emi = new EmployeeServiceImpl();
      List<EmployeeDto> lemD = emi.replaceAge((List<Employee>) emt);
      List<EmployeeDto> allAge = emi.findAllAge();
      assertEquals(lemD,allAge);
    }

    @Test
    public void teatFindAge() {



    }
}