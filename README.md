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
    @Test
    public void testReplaceAge() throws Exception {
        EmployeeServiceImpl emi = new EmployeeServiceImpl();
        Employee emt = new Employee();
        emt.setAge(10);
        emt.setEmId(10);
        emt.setEmName("测试");
        ArrayList<Employee> eml = Lists.newArrayList();
        eml.add(emt);

        Object replaceAge = Whitebox.invokeMethod(emi, "replaceAge", eml);

        EmployeeDto emDto = new EmployeeDto();
        emDto.setAge("少年");
        emDto.setEmId(10);
        emDto.setEmName("测试");
        ArrayList<EmployeeDto> emDtos = Lists.newArrayList();
        emDtos.add(emDto);
        assertEquals(emDtos,replaceAge);
        
        
	//测试获取索引 ，判断其是否存在
	@Test
	void testExistIndex() throws IOException {
		GetIndexRequest request  = new GetIndexRequest("kuang_index");
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
		System.out.println(exists);
	}

	//测试删除索引
    @Test
	void testDeleteIndex() throws IOException {
		DeleteIndexRequest request = new DeleteIndexRequest("kuang_index");
		AcknowledgedResponse delete = client.indices().delete(request,RequestOptions.DEFAULT);
		System.out.print(delete.isAcknowledged());
	}

	//测试添加文档
	@Test
	void testAddDoc() throws IOException {
		//创建对象
		User user = new User("狂神说",3);

		//创建请求
		IndexRequest request = new IndexRequest("kuang_index");

		//规则  put /kuang_index/_doc/1
		request.id("1");
		request.timeout(TimeValue.timeValueSeconds(1));
		request.timeout("1s");

		//将我们的数据放入请求

	 request.source(JSON.toJSONString(user), XContentType.JSON);

		//客户端发送请求
		IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
		System.out.println(indexResponse.toString());
		System.out.println(indexResponse.status()); //对应我们命令返回的状态

	}


```
