This is my first project!!!


```java
 @Test
    public void testFindAllAge() throws Exception {
        //mock数据访问 模拟 List<Employee> all = employeeRepository.findAll()
      EmployeeRepository emr = PowerMockito.mock(EmployeeRepository.class);
      Employee emt = new Employee();
      emt.setAge(10);
      emt.setEmId(10);
      emt.setEmName("测试");
      ArrayList<Employee> employee = Lists.newArrayList();
      employee.add(emt);
      PowerMockito.when(emr.findAll()).thenReturn(employee);
        //调用测试类
        EmployeeServiceImpl emi = new EmployeeServiceImpl();
        List<EmployeeDto> allAge = emi.findAllAge();

      //构造正确数据
      EmployeeDto emDto = new EmployeeDto();
      emDto.setAge("少年");
      emDto.setEmId(10);
      emDto.setEmName("测试");
      ArrayList<EmployeeDto> employeeDtos = Lists.newArrayList();
      employeeDtos.add(emDto);
      //断言
      assertEquals(employeeDtos,allAge);
    }
```
