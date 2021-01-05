package com.demo;

import ch.qos.logback.core.joran.spi.DefaultClass;
import com.alibaba.fastjson.JSON;
import com.demo.pojo.*;
import com.demo.utils.ESconst;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class DemoEsApiApplicationTests {

	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;

	//测试索引的创建
	@Test
	void  testCreatIndex(String index) throws IOException {
		//1.创建索引请求
		CreateIndexRequest request = new CreateIndexRequest(index);
		//2.客户端执行请求
		CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(createIndexResponse);
	}


	//测试获取索引 ，判断其是否存在
	@Test
	boolean testExistIndex(String index) throws IOException {
		GetIndexRequest request  = new GetIndexRequest(index);
		return client.indices().exists(request, RequestOptions.DEFAULT);

	}

	//测试删除索引
	@Test
	void testDeleteIndex(String index) throws IOException {
		DeleteIndexRequest request = new DeleteIndexRequest(index);
		AcknowledgedResponse delete = client.indices().delete(request,RequestOptions.DEFAULT);
		System.out.print(delete.isAcknowledged());
	}

	//测试添加文档
	@Test
	void testAddDoc() throws IOException {
		//创建对象
		User2 user = new User2("joey",6);

		//创建请求
		IndexRequest request = new IndexRequest("test_index");

		//规则  put /test_index/_doc/1
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

	@Test
	void testIsExist() throws IOException {
		//获取文档，判断是否存在
		GetRequest getRequest = new GetRequest("agg_index","1");

		//不获取返回的 _source 的上下文 ???
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		getRequest.storedFields("_noen_");

		boolean exists = client.exists(getRequest,RequestOptions.DEFAULT);
		System.out.println(exists);
	}
   //获取文档内容
	@Test
	void testGetDocument() throws  IOException {
		GetRequest getRequest = new GetRequest("test_index","1");
		GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
		System.out.println(getResponse.getSourceAsString()); //打印文档内容 Source
		System.out.println(getResponse);//返回全部内容
	}
    //更新文档信息
	@Test
	void testUpdateRequest() throws IOException {
		UpdateRequest updateRequest= new UpdateRequest("test_index", "1");
		updateRequest.timeout("1s");

		User user = new User(1596443295168L,57,"Query entity by key",200,"","Thread Group 1-1","text",true,"",1864,312,2,2,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')",54,0,27);

		updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);

		UpdateResponse updateResponse = client.update(updateRequest,RequestOptions.DEFAULT);

		System.out.println(updateResponse.status());
	}

	//删除文档记录
	@Test
	void testDeleteRequest() throws IOException {
		DeleteRequest request = new DeleteRequest("timetable_index","10000");
		request.timeout("1s");
		DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
		System.out.println(deleteResponse.status());
	}

	//特殊的，批量插入数据

	/**
	 *
	 * apm: http://10.58.15.16:8200
	 *
	 * es: http://10.58.15.16:9200
	 *
	 * kibana: http://10.58.15.16:5601
	 *
	 */

	@Test
	void testBulkRequest() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");

		ArrayList<User> userList =new ArrayList<>();
		userList.add(new User(1596443295168L,57,"Query entity by key",200,"","Thread Group 1-1","text",true,"",1864,312,2,2,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')",54,0,27));
		userList.add(new User(1596443295924L,17,"Query entity by key",200,"","Thread Group 1-2","text",true,"",1864,312,2,2,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')",17,0,1));
		userList.add(new User(1596443296564L,20,"Query entity by expand",200,"","Thread Group 1-1","text",true,"",3370,332,3,3,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')?$expand=perfTestNav",20,0,0));
		userList.add(new User(1596443296821L,17,"Query entity by key",200,"","Thread Group 1-3","text",true,"",1864,312,3,3,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')",16,0,1));
		userList.add(new User(1596443297032L,21,"Query entity by expand",200,"","Thread Group 1-2","text",true,"",3370,332,3,3,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')?$expand=perfTestNav",21,0,0));
		userList.add(new User(1596443297738L,20,"Query entity by expand",200,"","Thread Group 1-3","text",true,"",3370,332,4,4,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')?$expand=perfTestNav",20,0,0));
		userList.add(new User(1596443297807L,74,"Query entity with filter, top, skip, orderby",200,"","Thread Group 1-2","text",true,"",549193,368,4,4,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity?$filter=p0%20eq%20'hello'&$top=200&$skip=10&$orderby=p0%20desc",70,0,0));
		userList.add(new User(1596443297967L,18,"Query entity by key",200,"","Thread Group 1-4","text",true,"",1864,312,4,4,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity('abc')",18,0,1));
		userList.add(new User(1596443298010L,71,"Query entity with filter, top, skip, orderby",200,"","Thread Group 1-1","text",true,"",549193,368,4,4,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity?$filter=p0%20eq%20'hello'&$top=200&$skip=10&$orderby=p0%20desc",69,0,2));
		userList.add(new User(1596443298638L,25,"Insert entity",201,"","Thread Group 1-2","text",true,"",2324,3402,5,5,"http://10.58.15.82:9000/odata/v4/Perf.svc/PerfTestEntity",25,0,4));
//		System.out.println(userList);
		for (int i =0; i<userList.size();i++) {
			//批量更新和批量删除就在这里修改对应的请求就行
			bulkRequest.add(
					new IndexRequest("add_index")
					.id(""+(i+1))
					.source(JSON.toJSONString(userList.get(i)),XContentType.JSON));
		}
		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		System.out.println(bulkResponse.hasFailures()); //是否失败， 返回 false 代表成功
	}

	//查询
	@Test
	void clientSearch() throws IOException {

		SearchRequest searchRequest = new SearchRequest(ESconst.Es_INDEX);

		//构建搜索条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder

		//查询条件，使用QueryBuilders 工具来实现  termQueryBuilder 精确查询

		//QueryBuilders.matchAllQuery()
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("@timestamp", "2020-09-16T05:59:30.326Z");
		searchSourceBuilder.query(termQueryBuilder);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		for (SearchHit documentFields : searchResponse.getHits().getHits()) {
			System.out.println(documentFields.getSourceAsMap());
		}
	}


	//typical_success_example
	@Test
	void testWrapperSearch() throws IOException {
		SearchRequest searchRequest = new SearchRequest(ESconst.Es_INDEX);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query ="{\"match_all\": {}}";
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
		for(SearchHit hit : hits){
			String content = hit.getSourceAsString();
			System.out.println(content);
		}
	}




	//test_agg_wrapper_error
	@Test
	void aggWrapperSearch() throws IOException {
		SearchRequest searchRequest = new SearchRequest("test_index1");
		// add the query part
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query ="{\"match_all\": {}}";
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));


		// add the aggregation part
		AvgAggregationBuilder avgAgg = AggregationBuilders.avg("avg1").field("elapsed");
		searchSourceBuilder.aggregation(avgAgg);

		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
		for(SearchHit hit : hits){
			String content = hit.getSourceAsString();
			System.out.println(content);
		}
	}

	//test_success_example
	@Test
	void aWrapperSearch() throws IOException {
		SearchRequest searchRequest = new SearchRequest("test_index1");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query ="{\"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"order\": {\n" +
				"                \"gte\": 19,\n" +
				"                \"lte\": 20\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }";
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
		for(SearchHit hit : hits){
			String content = hit.getSourceAsString();
			System.out.println(content);
		}
	}

	//success_get the "aggregate_report_min" in every doc filter by time range
	@Test
	void baselineSearch() throws IOException {
		SearchRequest searchRequest = new SearchRequest("agg_index");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query ="{\n" +
				"      \"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"@timestamp\": {\n" +
				"                \"gte\": \"2020-09-16T05:59:30.326Z\",\n" +
				"                \"lte\": \"2020-09-16T05:59:30.330Z\"\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }";
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
		System.out.println("docNum:"+hits.length);
		for(SearchHit hit : hits){
			Object aggregate_report_min = hit.getSourceAsMap().get("aggregate_report_min");
			System.out.println(aggregate_report_min);
		}
	}

	@Test
	void  testpreCreatIndex() throws IOException {
		//1.创建索引请求
		CreateIndexRequest request = new CreateIndexRequest("agg_index2");
		//2.客户端执行请求
		CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(createIndexResponse);
	}

	//query docs from agg_index1 and save them in new agg_index2 success

	@Test
	void  currentDataSave() throws IOException {
		SearchRequest searchRequest = new SearchRequest("agg_index1");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query ="{\n" +
				"      \"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"@timestamp\": {\n" +
				"                \"gte\": \"2020-09-23T09:26:18.524Z\",\n" +
				"                \"lte\": \"2020-09-23T09:26:18.527Z\"\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }";
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
		System.out.println("docNum:"+hits.length);
		for(SearchHit hit : hits){
			System.out.println(hit.toString());
		}
		System.out.println("#############################################");
		ArrayList<CommonUser> cuserList =new ArrayList<>();

		for(SearchHit hit : hits){
//			long order = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("order")));
//			System.out.println(order);
			long order = Long.parseLong(hit.getSourceAsMap().get("order").toString());
			String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
			long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
			long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
			long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
			long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
			long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
			long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
			long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
			Double aggregate_report_rate = Double.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
			Double aggregate_report_bandwidth = Double.valueOf(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
			Double aggregate_report_stddev = Double.valueOf(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));


			cuserList.add(new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev));

		}
//		System.out.println(cuserList);
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");
		for (int i =0; i<cuserList.size();i++) {
			//批量更新和批量删除就在这里修改对应的请求就行
			bulkRequest.add(
					new IndexRequest("agg_index2")
							.id(""+(i+1))
							.source(JSON.toJSONString(cuserList.get(i)),XContentType.JSON));
		}
		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		System.out.println(bulkResponse.hasFailures()); //是否失败， 返回 false 代表成功

	}

	@Test
	void  finalTest() throws  Exception {
		SearchRequest curSearchRequest = new SearchRequest("agg_index");
		SearchSourceBuilder curSearchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String curQuery ="{\n" +
				"      \"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"@timestamp\": {\n" +
				"                \"gte\": \"2020-09-23T08:11:47.503Z\",\n" +
				"                \"lte\": \"2020-09-23T08:11:47.505Z\"\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }";
		WrapperQueryBuilder curWrapQB = new WrapperQueryBuilder(curQuery);
		curSearchSourceBuilder.query(curWrapQB);
		curSearchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		curSearchRequest.source(curSearchSourceBuilder);
		SearchResponse curSearchResponse = client.search(curSearchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(curSearchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] curhits = curSearchResponse.getHits().getHits();
		System.out.println("docNum:"+curhits.length);
		for(SearchHit hit : curhits){
			System.out.println(hit.toString());
		}
	}

//	##################################### version-0 ###############################################################

	//final calculate and save all data in agg_index3
	@Test
	void  calDiffer() throws Exception {
		//get baselineData from agg_index1
		SearchRequest searchRequest = new SearchRequest("agg_index1");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query ="{\n" +
				"      \"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"@timestamp\": {\n" +
				"                \"gte\": \"2020-09-23T09:26:18.524Z\",\n" +
				"                \"lte\": \"2020-09-23T09:26:18.527Z\"\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }";
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
		System.out.println("docNum:"+hits.length);
		for(SearchHit hit : hits){
			System.out.println(hit.toString());
		}
		System.out.println("#############################################");
//		ArrayList<CommonUser> baselineList =new ArrayList<>();
		HashMap<String, CommonUser> baseMap=new HashMap<>();
		for(SearchHit hit : hits){
			long order = Long.parseLong(hit.getSourceAsMap().get("order").toString());
			String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
			long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
			long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
			long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
			long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
			long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
			long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
			long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
			Double aggregate_report_rate = Double.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
			Double aggregate_report_bandwidth = Double.valueOf(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
			Double aggregate_report_stddev = Double.valueOf(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));
//			baselineList.add(new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev));
			CommonUser tmpUsr =new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev);
			baseMap.put(sampler_label,tmpUsr);
		}

		//get curData from agg_index
		SearchRequest curSearchRequest = new SearchRequest("agg_index");
		SearchSourceBuilder curSearchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String curQuery ="{\n" +
				"      \"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"@timestamp\": {\n" +
				"                \"gte\": \"2020-09-23T08:11:47.503Z\",\n" +
				"                \"lte\": \"2020-09-23T08:11:47.506Z\"\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }";
		WrapperQueryBuilder curWrapQB = new WrapperQueryBuilder(curQuery);
		curSearchSourceBuilder.query(curWrapQB);
		curSearchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		curSearchRequest.source(curSearchSourceBuilder);
		SearchResponse curSearchResponse = client.search(curSearchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(curSearchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] curhits = curSearchResponse.getHits().getHits();
		System.out.println("docNum:"+curhits.length);
		for(SearchHit hit : curhits){
			System.out.println(hit.toString());
		}
		System.out.println("#############################################");
//		ArrayList<CommonUser> curList =new ArrayList<>();
		HashMap<String, CommonUser> curMap=new HashMap<String, CommonUser>();
		for(SearchHit hit : curhits){
			long order = Long.parseLong(hit.getSourceAsMap().get("order").toString());
			String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
			long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
			long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
			long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
			long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
			long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
			long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
			long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
			Double aggregate_report_rate = Double.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
			Double aggregate_report_bandwidth = Double.valueOf(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
			Double aggregate_report_stddev = Double.valueOf(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));
//			curList.add(new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev));
			CommonUser tmpUsr =new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev);
			curMap.put(sampler_label,tmpUsr);
		}

		if (curhits.length != hits.length) {
				throw  new Exception("baseLine and current doc_Nums are not equal ");
		}else{
			ArrayList<DifferUser> difList =new ArrayList<>();
			for (String key : curMap.keySet()) {
				CommonUser Cuser = curMap.get(key);
			    CommonUser Buser = baseMap.get(key);

			    String Url = Cuser.getSampler_label();
			    long count1 = Cuser.getAggregate_report_count();
			    long avg1  = Cuser.getAverage();
			    long median1 = Cuser.getAggregate_report_median();
			    long P901 = Cuser.getAggregate_report_90();
			    long min1 = Cuser.getAggregate_report_min();
			    long max1 = Cuser.getAggregate_report_max();
			    long error1 = Cuser.getAggregate_report_error();
			    double rate1 = Cuser.getAggregate_report_rate();
			    double bandwidth1 = Cuser.getAggregate_report_bandwidth();
			    double std1 = Cuser.getAggregate_report_std();


				long count2 = Buser.getAggregate_report_count();
				long avg2  = Buser.getAverage();
				long median2 = Buser.getAggregate_report_median();
				long P902 = Buser.getAggregate_report_90();
				long min2 = Buser.getAggregate_report_min();
				long max2 = Buser.getAggregate_report_max();
				long error2 = Buser.getAggregate_report_error();
				double rate2 = Buser.getAggregate_report_rate();
				double bandwidth2 = Buser.getAggregate_report_bandwidth();
				double std2 = Buser.getAggregate_report_std();


				long count3 = count1-count2;
				long avg3  = avg1-avg2;
				long median3 = median1-median2;
				long P903 = P901-P902;
				long min3 =min1-min2;
				long max3 = max1-max2;
				long error3 = error1-error2;
				double rate3 = rate1-rate2;
				double bandwidth3 = bandwidth1-bandwidth2;
				double std3 = std1-std2;

				difList.add(new DifferUser(Url,count1,avg1,median1,P901,min1,max1,error1,rate1,bandwidth1,std1,count2,avg2,median2,P902,min2,max2,error2,rate2,bandwidth2,std2,count3,avg3,median3,P903,min3,max3,error3,rate3,bandwidth3,std3));
			}

			//if agg_index3 exist ,update agg_index3 else create agg_index3
			if (testExistIndex("agg_index3")) {
				testDeleteIndex("agg_index3");
			}else{
				testCreatIndex("agg_index3");
			}

			//BulkRequest 批量导入
			BulkRequest bulkRequest = new BulkRequest();
			bulkRequest.timeout("10s");
			for (int i =0; i<difList.size();i++) {
				//批量更新和批量删除就在这里修改对应的请求就行
				bulkRequest.add(
						new IndexRequest("agg_index3")
								.id(""+(i+1))
								.source(JSON.toJSONString(difList.get(i)),XContentType.JSON));
			}
			BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
			if (!bulkResponse.hasFailures()) {
				System.out.println("save in es success");
			}else {
				System.out.println("save failure");
			}

		}
	}

	//######################################-new version1-######################################################


	@Test
	void  timeTableInsert(double stTime,double edTime, long inputId,String timeTableIndex) throws Exception {

		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");
		bulkRequest.add(
				new IndexRequest(timeTableIndex)
					.id(""+inputId)
					.source(JSON.toJSONString(new TimeTable(stTime,edTime,inputId)),XContentType.JSON));

		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		System.out.println(bulkResponse.hasFailures()); //是否失败， 返回 false 代表成功
	}

    //!!!!! inputCnt++ problem
//	static public long inputCnt;
	@Test
//	String idx,String config_path ,String config_name
	void inputData( ) throws Exception {
		//save data in "agg_index_v2" ,conf in logstash_default.conf
		Runtime run=Runtime.getRuntime();
		try {
//			"agg_index"
			long tmpCnt = numOfDoc("agg_index_v2");

			//input
			run.exec("cmd /k start C:\\Users\\I524987\\Desktop\\dashboard\\logstash-7.6.2\\bin\\logstash -f C:\\Users\\I524987\\Desktop\\dashboard\\logstash-7.6.2\\bin\\logstash_default.conf");

//			run.exec("cmd /k start "+config_path+"\\logstash -f "+config_path+"\\"+config_name);
			//4min could support 0.2 millis data;
			Thread.sleep(60000 * 1);
			//notice that the  num of two times input doc must be same,then the tmpCn*2 == ... is valid.
			if ( numOfDoc("agg_index_v2")-tmpCnt == 7) {
				InputUtil.cnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("inputData error");
		}

		System.out.println(InputUtil.cnt);
		System.out.println(numOfDoc("agg_index_v2"));
	}


	//注意此处的time_filed_name 的选择会有不同，agg_index_v2 时，time_filed_name is @timestamp
	//对于其他的index时，其time_filed_name 可能不是@timestamp
	double tmpMinTimestamp;
	@Test
	double getMinTimeStamp(String idx,String time_filed_name) throws Exception {

//			MinAggregationBuilder aggregationBuilder = AggregationBuilders.min("minTimestamp").field("@timestamp");
			MinAggregationBuilder aggregationBuilder = AggregationBuilders.min("minTimestamp").field(time_filed_name);

			SearchRequest searchRequest = new SearchRequest(idx);//限定index
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.aggregation(aggregationBuilder);
			searchSourceBuilder.size(0);//设置不需要文档数据，只需要返回聚合结果
			searchRequest.source(searchSourceBuilder);

			SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);

			System.out.println("____________");
			System.out.println("run in getMinTimeStamp() :");
			System.out.println(searchResponse);
			System.out.println("____________");


			//统计结果

			Aggregations aggregations = searchResponse.getAggregations();
			Map<String, Aggregation> aggregationMap = aggregations.asMap();
			double res = 0;
			for(Map.Entry<String,Aggregation> each: aggregationMap.entrySet()) {
				tmpMinTimestamp = ((ParsedMin) (each.getValue())).getValue();
				res = tmpMinTimestamp;
			}
			return res;
	}




	double tmpMaxTimestamp;
	@Test
	double getMaxTimeStamp(String idx) throws Exception {

		MaxAggregationBuilder aggregationBuilder = AggregationBuilders.max("maxTimestamp").field("@timestamp");

		SearchRequest searchRequest = new SearchRequest(idx);//限定index
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.aggregation(aggregationBuilder);
		searchSourceBuilder.size(0);//设置不需要文档数据，只需要返回聚合结果
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
		System.out.println(searchResponse);


		//统计结果
		Aggregations aggregations = searchResponse.getAggregations();
		Map<String, Aggregation> aggregationMap = aggregations.asMap();
		double res = 0;
		for(Map.Entry<String,Aggregation> each: aggregationMap.entrySet()) {
			tmpMaxTimestamp = ((ParsedMax) (each.getValue())).getValue();
			res = tmpMaxTimestamp;
		}
        return res;
	}


	//return the nums of agg_index
	@Test
	long numOfDoc(String idx) throws IOException {
		SearchRequest searchRequest = new SearchRequest(idx);//限定index

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
		searchSourceBuilder.query(matchAllQueryBuilder);
		searchSourceBuilder.size(0);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		TotalHits totalHits = searchResponse.getHits().getTotalHits();
		return totalHits.value;
	}

	/**
	 *
	 * to do 1
	 *c
	 *
	 */

    //to do / not test
	//get time tange for pro timetable,and use the range to get Mintimestampe and maxtimestamp
	//problem inputId can't more than 8
	@Test
	double searchProEndTime(long inputId) throws IOException {

		SearchRequest searchRequest = new SearchRequest("timetable_index");

		//构建搜索条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder

		//查询条件，使用QueryBuilders 工具来实现  termQueryBuilder 精确查询
		//QueryBuilders.matchAllQuery()
		//inputCnt-1;
		String num = String.valueOf(inputId-1);
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("inputId", num);
		searchSourceBuilder.query(termQueryBuilder);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        double res =0.0;
		for (SearchHit documentFields : searchResponse.getHits().getHits()) {
			res = Double.parseDouble(documentFields.getSourceAsMap().get("endTimeStamp").toString());
		}
		return res;
	}

	@Test
	void searchProEndTime_test() throws IOException {
		System.out.println(searchProEndTime(9));
	}

	//save the doc with timestamp in a tmp timetable

	@Test
	Boolean tmpTimeTable_insert(SearchHit[] hits) throws IOException, ParseException {


		ArrayList<CommonObj> tmpTimeList = new ArrayList<>();


		for (SearchHit hit : hits) {

			String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
			long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
			long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
			long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
			long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
			long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
			long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
			long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
			double aggregate_report_rate = Double.parseDouble(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
			double aggregate_report_bandwidth = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
			double aggregate_report_stddev = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));
			String timestamp = hit.getSourceAsMap().get("@timestamp").toString();
			timestamp = timestamp.replace("Z", " UTC");//UTC是本地时间
			SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
			Date d = null;
			try {
				d = format.parse(timestamp);
			} catch (ParseException e) {
// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long time = d.getTime();

			System.out.println(time+"run in tmpTimeTable_insert()");

//			curList.add(new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev));
			CommonObj tmpObj =new CommonObj(sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev,time);
			tmpTimeList.add(tmpObj);

		}

		//if tmpTime_index exist ,update agg_index3 else create agg_index3
		if (testExistIndex("tmptime_index")) {
			testDeleteIndex("tmptime_index");
		}else{
			testCreatIndex("tmptime_index");
		}






		//BulkRequest 批量导入
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");
		for (int i =0; i<tmpTimeList.size();i++) {
			//批量更新和批量删除就在这里修改对应的请求就行
			bulkRequest.add(
					new IndexRequest("tmptime_index")
							.id(""+(i+1))
							.source(JSON.toJSONString(tmpTimeList.get(i)),XContentType.JSON));
		}
		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		return bulkResponse.hasFailures();
//		if (!bulkResponse.hasFailures()) {
//			System.out.println("save in es success");
//		}else {
//			System.out.println("save failure");
//		}
	}


	/**
	 * to do 2
	 * develop from here
	 */

	//***insert not be over
	//Timetable_insert plus
	/**
	 * 1.input a tmp table and getMInTimeStamp
	 * 2.the we get this goup inputId ,minTime and maxTime
	 * 3.exe timeTable_inset
	 */

	@Test
	void inputRecord_update() throws Exception {

			//inputData if success inputCnt++ else throw Exception and get the inputCnt
			inputData();

			//if timeTable_index exist , create timeTable_index
			if (!testExistIndex("timetable_index") && InputUtil.cnt==1) {
				testCreatIndex("timetable_index");
				timeTableInsert(getMinTimeStamp("agg_index_v2","@timestamp"),getMaxTimeStamp("agg_index_v2"),InputUtil.cnt,"timeTable_index");
			}else if (testExistIndex("timetable_index") && InputUtil.cnt > 1 ) {
				double v = searchProEndTime(InputUtil.cnt);
				SearchHit[] hits = searchByTimeRange("gt",String.valueOf(v),"lte", String.valueOf(getMaxTimeStamp("agg_index_v2")),"agg_index_v2");
				tmpTimeTable_insert(hits);

				timeTableInsert(getMinTimeStamp("tmptime_index","timestamp"),getMaxTimeStamp("agg_index_v2"),InputUtil.cnt,"timetable_index");
			}

	}


	//not test
	//range is (a,b]
	//time range : gte lte gt lt
	@Test
	SearchHit[]  searchByTimeRange(String timeUp_bound,String timeUp,String timeLow_bound,String timeLow,String idx) throws IOException {
		SearchRequest searchRequest = new SearchRequest(idx);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query =String.format("{\n" +
				"      \"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"@timestamp\": {\n" +
				"                \"%s\": %s ,\n" + // gt dif from gte
				"                \"%s\": %s \n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }",timeUp_bound,timeUp,timeLow_bound,timeLow);
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);
 		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
//		System.out.println("docNum:"+hits.length);
//		for(SearchHit hit : hits){
//			System.out.println(hit.toString());
//		}
//		System.out.println("#############################################");
		return hits;
	}


	//return the obj in the TimeTable
	@Test
	SearchHit[] searchById(long Id) throws Exception {
		SearchRequest searchRequest = new SearchRequest("timetable_index");

		//构建搜索条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder

		//查询条件，使用QueryBuilders 工具来实现  termQueryBuilder 精确查询
		//QueryBuilders.matchAllQuery()
		//inputCnt-1;
		String num = String.valueOf(Id);
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("inputId", num);
		searchSourceBuilder.query(termQueryBuilder);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		SearchHit[] hits = searchResponse.getHits().getHits();

		return hits;

//		double res =0.0;
//		for (SearchHit documentFields : searchResponse.getHits().getHits()) {
//			double endTimeStamp = Double.valueOf(documentFields.getSourceAsMap().get("endTimeStamp").toString());
//			res = endTimeStamp;
//		}
//		return res;

	}


	@Test
	void  calAfSearch(SearchHit[] bseHits, SearchHit[] curHits) throws Exception{


		HashMap<String, CommonObj> baseMap=new HashMap<>();
		for(SearchHit hit : bseHits){
			String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
			long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
			long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
			long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
			long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
			long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
			long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
			long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
			double aggregate_report_rate = Double.parseDouble(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
			double aggregate_report_bandwidth = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
			double aggregate_report_stddev = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));
//			long timestamp = Long.parseLong(hit.getSourceAsMap().get("@timestamp").toString());
//			baselineList.add(new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev));
			CommonObj tmpUsr =new CommonObj(sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev,404);
			baseMap.put(sampler_label,tmpUsr);

		}



		HashMap<String, CommonObj> curMap=new HashMap<>();
		for(SearchHit hit : curHits){
			String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
			long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
			long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
			long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
			long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
			long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
			long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
			long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
			double aggregate_report_rate = Double.parseDouble(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
			double aggregate_report_bandwidth = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
			double aggregate_report_stddev = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));
//			long timestamp = Long.parseLong(hit.getSourceAsMap().get("@timestamp").toString());
//			curList.add(new CommonUser(order,sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev));
			CommonObj tmpUsr =new CommonObj(sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev,404);
			curMap.put(sampler_label,tmpUsr);
		}

		if (curHits.length != bseHits.length) {
			throw  new Exception("baseLine and current doc_Nums are not equal ");
		}else{
			ArrayList<DifferUser> difList =new ArrayList<>();
			for (String key : curMap.keySet()) {
				CommonObj Cuser = curMap.get(key);
				CommonObj Buser = baseMap.get(key);


				String Url = Cuser.getSampler_label();
				long count1 = Cuser.getAggregate_report_count();
				long avg1  = Cuser.getAverage();
				long median1 = Cuser.getAggregate_report_median();
				long P901 = Cuser.getAggregate_report_90();
				long min1 = Cuser.getAggregate_report_min();
				long max1 = Cuser.getAggregate_report_max();
				long error1 = Cuser.getAggregate_report_error();
				double rate1 = Cuser.getAggregate_report_rate();
				double bandwidth1 = Cuser.getAggregate_report_bandwidth();
				double std1 = Cuser.getAggregate_report_std();


				long count2 = Buser.getAggregate_report_count();
				long avg2  = Buser.getAverage();
				long median2 = Buser.getAggregate_report_median();
				long P902 = Buser.getAggregate_report_90();
				long min2 = Buser.getAggregate_report_min();
				long max2 = Buser.getAggregate_report_max();
				long error2 = Buser.getAggregate_report_error();
				double rate2 = Buser.getAggregate_report_rate();
				double bandwidth2 = Buser.getAggregate_report_bandwidth();
				double std2 = Buser.getAggregate_report_std();


				long count3 = count1-count2;
				long avg3  = avg1-avg2;
				long median3 = median1-median2;
				long P903 = P901-P902;
				long min3 =min1-min2;
				long max3 = max1-max2;
				long error3 = error1-error2;
				double rate3 = rate1-rate2;
				double bandwidth3 = bandwidth1-bandwidth2;
				double std3 = std1-std2;

				difList.add(new DifferUser(Url,count1,avg1,median1,P901,min1,max1,error1,rate1,bandwidth1,std1,count2,avg2,median2,P902,min2,max2,error2,rate2,bandwidth2,std2,count3,avg3,median3,P903,min3,max3,error3,rate3,bandwidth3,std3));
			}

			//if agg_index3 exist ,update agg_index3 else create agg_index3
			if (testExistIndex("agg_index3")) {
				testDeleteIndex("agg_index3");
			}else{
				testCreatIndex("agg_index3");
			}

			//BulkRequest 批量导入
			BulkRequest bulkRequest = new BulkRequest();
			bulkRequest.timeout("10s");
			for (int i =0; i<difList.size();i++) {
				//批量更新和批量删除就在这里修改对应的请求就行
				bulkRequest.add(
						new IndexRequest("agg_index3")
								.id(""+(i+1))
								.source(JSON.toJSONString(difList.get(i)),XContentType.JSON));
			}
			BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
			if (!bulkResponse.hasFailures()) {
				System.out.println("save in es success");
			}else {
				System.out.println("save failure");
			}

		}
	}


//	@Test
//	void IdChange(long id1,long id2) {
//
//	}

	//final job go!!
	//get baselineTime and currentTime from TimeTable
	//if you don't want to set the baseline ,set baseline_Id as -1,the the baseline_Id will be setted as current_id -1



	@Test
	void  calDiffertest_firstversion(long baseline_Id, long current_Id) throws Exception {
		double baselineStartTime = 0;
		double baselineEndTime =0;
		double currentStartTime = 0;
		double currentEndTime = 0;

		long doc_nums;
		doc_nums = numOfDoc("timetable_index");
		//if baseline_Id == -1 ,set baseline_Id = current_id -1
		if (baseline_Id ==-1 && current_Id <= doc_nums && current_Id > 1) {
			baseline_Id = current_Id - 1;
			SearchHit[] searchHits_bse = searchById(baseline_Id);
			for (SearchHit documentFields_bse : searchHits_bse) {
				baselineStartTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("startTimeStamp").toString());
				baselineEndTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("endTimeStamp").toString());
			}
			SearchHit[] searchHits_cur = searchById(current_Id);
			for (SearchHit documentFields_cur : searchHits_cur) {
				currentStartTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("startTimeStamp").toString());
				currentEndTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("endTimeStamp").toString());
			}
			SearchHit[] agg_final_hits_bse = searchByTimeRange("gte",String.valueOf(baselineStartTime), "lte",String.valueOf(baselineEndTime), "agg_index_v2");
			SearchHit[] agg_final_hits_cur = searchByTimeRange("gte",String.valueOf(currentStartTime),"lte", String.valueOf(currentEndTime), "agg_index_v2");
			calAfSearch(agg_final_hits_bse,agg_final_hits_cur);

		}else if (baseline_Id >0 && current_Id <= doc_nums && current_Id > 1) {
			if (baseline_Id < current_Id){
			//operate based on baseline_Id && current_Id
				SearchHit[] searchHits_bse = searchById(baseline_Id);
				for (SearchHit documentFields_bse : searchHits_bse) {
					baselineStartTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("startTimeStamp").toString());
					baselineEndTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("endTimeStamp").toString());
				}
				SearchHit[] searchHits_cur = searchById(current_Id);
				for (SearchHit documentFields_cur : searchHits_cur) {
					currentStartTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("startTimeStamp").toString());
					currentEndTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("endTimeStamp").toString());
				}
				SearchHit[] agg_final_hits_bse = searchByTimeRange("gte",String.valueOf(baselineStartTime), "lte",String.valueOf(baselineEndTime), "agg_index_v2");
				SearchHit[] agg_final_hits_cur = searchByTimeRange("gte",String.valueOf(currentStartTime), "lte",String.valueOf(currentEndTime), "agg_index_v2");
				calAfSearch(agg_final_hits_bse,agg_final_hits_cur);
			}else{
				throw new Exception("baseline_Id >= current_id");
			}
		}else{
			  	throw new Exception("invalid idInput");
		}

	}


	@Test
	void  calDiffertest(long baseline_Id, long current_Id) throws Exception {
		double baselineStartTime = 0;
		double baselineEndTime =0;
		double currentStartTime = 0;
		double currentEndTime = 0;

		long doc_nums;
		doc_nums = numOfDoc("timetable_index");
		//if baseline_Id == -1 ,set baseline_Id = current_id -1
		if (doc_nums >1 && baseline_Id>0 && baseline_Id <=doc_nums && current_Id <= doc_nums && current_Id > 1 && baseline_Id < current_Id) {

			SearchHit[] searchHits_bse = searchById(baseline_Id);
			for (SearchHit documentFields_bse : searchHits_bse) {
				//baselineStartTime null 导致下一步的函数调用出错
				baselineStartTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("startTimeStamp").toString());
				baselineEndTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("endTimeStamp").toString());
			}
			SearchHit[] searchHits_cur = searchById(current_Id);
			for (SearchHit documentFields_cur : searchHits_cur) {
				//此处timetable表中的startTimeStamp:1,608,620,615,880 时 会出现转化丢失0的情况是的时间戳变成startTimeStamp:1,608,620,615,88
				currentStartTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("startTimeStamp").toString());
				currentEndTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("endTimeStamp").toString());
			}

//			BigDecimal bigDecimal_curStart = bigdecimal_delete_E(currentStartTime);

			SearchHit[] agg_final_hits_bse = searchByTimeRange("gte",bigdecimal_delete_E(baselineStartTime)+"", "lte",bigdecimal_delete_E(baselineEndTime)+"", "agg_index_v2");
			SearchHit[] agg_final_hits_cur = searchByTimeRange("gte",bigdecimal_delete_E(currentStartTime)+"","lte", bigdecimal_delete_E(currentEndTime)+"", "agg_index_v2");
			calAfSearch(agg_final_hits_bse,agg_final_hits_cur);

		}else if (baseline_Id >0 && current_Id <= doc_nums && current_Id > 1) {
			if (baseline_Id < current_Id){
				//operate based on baseline_Id && current_Id
				SearchHit[] searchHits_bse = searchById(baseline_Id);
				for (SearchHit documentFields_bse : searchHits_bse) {
					baselineStartTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("startTimeStamp").toString());
					baselineEndTime = Double.parseDouble(documentFields_bse.getSourceAsMap().get("endTimeStamp").toString());
				}
				SearchHit[] searchHits_cur = searchById(current_Id);
				for (SearchHit documentFields_cur : searchHits_cur) {
					currentStartTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("startTimeStamp").toString());
					currentEndTime = Double.parseDouble(documentFields_cur.getSourceAsMap().get("endTimeStamp").toString());
				}
				SearchHit[] agg_final_hits_bse = searchByTimeRange("gte",bigdecimal_delete_E(baselineStartTime)+"", "lte",bigdecimal_delete_E(baselineEndTime)+"", "agg_index_v2");
				SearchHit[] agg_final_hits_cur = searchByTimeRange("gte",bigdecimal_delete_E(currentStartTime)+"","lte", bigdecimal_delete_E(currentEndTime)+"", "agg_index_v2");
				calAfSearch(agg_final_hits_bse,agg_final_hits_cur);
			}else{
				throw new Exception("baseline_Id >= current_id");
			}
		}else{
			throw new Exception("invalid idInput");
		}

	}

	/**
	 *
	 * @param num
	 * @return bg
	 *
	 * 本函数用来处理聪es中查询出的时间戳，将double 类型的时间戳转换成
	 */
	@Test
	BigDecimal bigdecimal_delete_E(double num) {
		BigDecimal bg = new BigDecimal(num);
//		BigDecimal bigDecimal = BigDecimal.valueOf(num);

		return bg;
	}

//	@Test
//	void delete_E_res() {
//		System.out.println(bigdecimal_delete_E(1.6022061588E12));
//	}




	@Test
	void calById() throws Exception {
		calDiffertest(2,3);
//		calDiffertest_firstversion(2,3);
	}

	/**
	 * 1.参数修改面临的值传递问题-waiting
	 * 2.在字符串中添加变量的可行性-ok
	 * 3.searchByRange 中取的是左开右闭
	 * 4.注意时间格式母目前为Linux时间戳，类型为double, 所以作为Warp函数参数的时候是否会出现DSL失效的情况-ok
	 * 5.timeTableInsert test......
	 * */



	//字符串参数的有效性
	//Linux日期作为dsl语句参数的可行性测试
	//增加了参数，再次测一下

	@Test
	void stringAgrTest() throws IOException {
		SearchRequest searchRequest = new SearchRequest("agg_index1");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder
		String query =String.format("{\n" +
				"      \"bool\": {\n" +
				"        \"must\": [\n" +
				"          {\n" +
				"             \"match_all\": {}\n" +
				"          }\n" +
				"        ],\n" +
				"        \"filter\": [\n" +
				"          {\n" +
				"            \"range\": {\n" +
				"              \"@timestamp\": {\n" +
				"                \"%s\": %s ,\n" + // gt dif from gte
				"                \"%s\": %s \n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        ]\n" +
				"      }\n" +
				"    }","gte","1602585318590","lte","1602585318593");
		WrapperQueryBuilder wrapQB = new WrapperQueryBuilder(query);
		searchSourceBuilder.query(wrapQB);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("============================================");
		SearchHit[] hits = searchResponse.getHits().getHits();
		System.out.println("docNum:"+hits.length);
	}

//	################################################ version-2 ######################################################################


//	String idx,String config_path ,String config_name
    @Test
	long imp_Csv( ) throws Exception {
		long inputCnt = 0;

		//save data in "agg_index_v2" ,conf in logstash_default.conf
		Runtime run = Runtime.getRuntime();
		try {
//			"agg_index"
			long tmpCnt = numOfDoc("agg_index_v2");

			//input
			run.exec("cmd /k start C:\\Users\\I524987\\Desktop\\dashboard\\logstash-7.6.2\\bin\\logstash -f C:\\Users\\I524987\\Desktop\\dashboard\\logstash-7.6.2\\bin\\logstash_default.conf");

//			run.exec("cmd /k start "+config_path+"\\logstash -f "+config_path+"\\"+config_name);
			//4min could support 0.2 millis data;
			Thread.sleep(60000 * 1);

//			Process listprocess = Runtime.getRuntime().exec("cmd.exe /c tasklist");
//
//			InputStream is = listprocess.getInputStream();
//
//			byte[] buf = new byte[256];
//
//			BufferedReader r = new BufferedReader(new InputStreamReader(is));
//
//
//
//			StringBuffer sb = new StringBuffer();
//
//			String str = null;
//
//			while ((str = r.readLine()) != null) {
//
//				String id = null;
//
//				Matcher matcher = Pattern.compile("logstash_default.conf" + "[ ]*([0-9]*)").matcher(str);
//
//				while (matcher.find()) {
//
//					if (matcher.groupCount() >= 1) {
//
//						id = matcher.group(1);
//
//						if (id != null) {
//
//							Integer pid = null;
//
//							try {
//
//								pid = Integer.parseInt(id);
//
//							} catch (NumberFormatException e) {
//
//								e.printStackTrace();
//
//							}
//
//							if (pid != null) {
//
//								Runtime.getRuntime().exec("cmd.exe /c taskkill /f /pid " + pid);
//
//								System.out.println("kill progress");
//							}

			//notice that the  num of two times input doc must be same,then the tmpCn*2 == ... is valid.
			long docNum = numOfDoc("agg_index_v2");

			if (numOfDoc("agg_index_v2") - tmpCnt == 7) {
				inputCnt = docNum / 7;

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("inputData error");
		}

//		System.out.println(inputCnt);
//		System.out.println(numOfDoc("agg_index_v2"));


		return inputCnt;

	}







//	@Test
//	void saveData() throws Exception {
//
//
//
//	}

	@Test
	void filterTableInsert(SearchHit[] hits,long inputCnt,double startTime,double endTime,String sdEndTime) throws Exception {

		System.out.println("run filter insert");
		ArrayList<FilterObj> filterList = new ArrayList<>();



		for (SearchHit hit : hits) {

			String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
			long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
			long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
			long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
			long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
			long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
			long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
			long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
			double aggregate_report_rate = Double.parseDouble(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
			double aggregate_report_bandwidth = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
			double aggregate_report_stddev = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));
			String timestamp = hit.getSourceAsMap().get("@timestamp").toString();

			FilterObj filterObj =new FilterObj(sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev,timestamp,startTime,endTime,inputCnt,sdEndTime);
			filterList.add(filterObj);



		}

		long num = numOfDoc("agg_index_v2");
		long cnt = num - 7 ;

		//BulkRequest 批量导入
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");
		for (int i =0; i<filterList.size();i++) {
			//批量更新和批量删除就在这里修改对应的请求就行
			bulkRequest.add(
					new IndexRequest("filter_index")
							.id(""+(i+1+cnt))
							.source(JSON.toJSONString(filterList.get(i)),XContentType.JSON));
		}
		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		if (!bulkResponse.hasFailures()) {
			System.out.println("save in filterTable success");
		}else {
			System.out.println("save filterTable failure");
		}
	}




    //inputRecord_update2 is used to input data to es and show the input_timestamp and input data in kibana every time
	@Test
	void inputRecord_update2() throws Exception { // 判断返回值的方式

		//inputData if success inputCnt++ else throw Exception and get the inputCnt
		long inputCnt= imp_Csv();
		System.out.println("the inputCnt is : ");
		System.out.println(inputCnt);
//		Thread.sleep(30000*1);
		//if timeTable_index exist , create timeTable_index
		if (!testExistIndex("timetable_index") && inputCnt==1) {
			testCreatIndex("timetable_index"); //自动创建
			double uptime = getMinTimeStamp("agg_index_v2","@timestamp");
			double lowtime = getMaxTimeStamp("agg_index_v2");
			String sdEndTime = getUnixTransferTime(lowtime);
			String mintime = bigdecimal_delete_E(getMinTimeStamp("agg_index_v2","@timestamp"))+"";
			String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
			timeTableInsert(uptime,lowtime,inputCnt,"timetable_index");
			SearchHit[] searchHits = searchByTimeRange("gte", mintime, "lte", maxtime, "agg_index_v2");
			filterTableInsert(searchHits,1,uptime,lowtime,sdEndTime);
		}else if (testExistIndex("timetable_index") && inputCnt > 1 ) {
			double v = searchProEndTime(inputCnt);//  问题： timetable 内inputid > 1 的doc 其startTime 为 null
			String mintime = bigdecimal_delete_E(v)+"";
			double timeLow = getMaxTimeStamp("agg_index_v2");
			String sdEndTime = getUnixTransferTime(timeLow);

			String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
			//如果出现tmptable里数据数目大于7为10，则有可能调用searchByTimeRange出问题，查询出的问题时hits长度过大，原因与
			//某个mintime太小相关
			SearchHit[] hits = searchByTimeRange("gt",mintime,"lte", maxtime,"agg_index_v2");

			// tmpTimeTable_insert(hits) errors ,java.lang.NumberFormatException: For input string: "2020-11-13T06:51:00.035Z"
			Boolean input_tmpTimeTable_success = tmpTimeTable_insert(hits); // 成功则返回failure

			if (!input_tmpTimeTable_success) {
				double uptime = getMinTimeStamp("tmptime_index","timestamp");
				System.out.println("print start in inputRecord_update2");
				System.out.println("!!!!!!!!!!!!!!startTime in timetable_index:  ");
				System.out.println(getMinTimeStamp("tmptime_index", "timestamp"));
				System.out.println(uptime);
				System.out.println("print over in inputRecord_update2");
//			double lowtime = getMaxTimeStamp("agg_index_v2");
				timeTableInsert(uptime,timeLow,inputCnt,"timetable_index");
				filterTableInsert(hits,inputCnt,uptime,timeLow,sdEndTime);
			}else{
				System.out.println("getMinTimeStamp(\"tmptime_index\",\"timestamp\") failure!!!!");
				//有待抛出异常
			}
//			//在input_tmp和getMinTimeStamp("tmptime_index","timestamp")之间，要注意 不加睡眠函数会出现 uptime值为Infinity的情况 导致 存入timetable的startime 为空
//			Thread.sleep(30000*1);

		}

	}


	//休眠方式解决查询插入冲突
	@Test
	void inputRecord_update3() throws Exception { //sleep 方式

		//inputData if success inputCnt++ else throw Exception and get the inputCnt
		long inputCnt= imp_Csv();
		System.out.println("the inputCnt is : ");
		System.out.println(inputCnt);
//		Thread.sleep(30000*1);
		//if timeTable_index exist , create timeTable_index
		if (!testExistIndex("timetable_index") && inputCnt==1) {
			testCreatIndex("timetable_index"); //自动创建
			double uptime = getMinTimeStamp("agg_index_v2","@timestamp");
			double lowtime = getMaxTimeStamp("agg_index_v2");
			String sdEndTime = getUnixTransferTime(lowtime);
			String mintime = bigdecimal_delete_E(getMinTimeStamp("agg_index_v2","@timestamp"))+"";
			String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
			timeTableInsert(uptime,lowtime,inputCnt,"timetable_index");
			SearchHit[] searchHits = searchByTimeRange("gte", mintime, "lte", maxtime, "agg_index_v2");
			filterTableInsert(searchHits,1,uptime,lowtime,sdEndTime);
		}else if (testExistIndex("timetable_index") && inputCnt > 1 ) {
			double v = searchProEndTime(inputCnt);//  问题： timetable 内inputid > 1 的doc 其startTime 为 null
			String mintime = bigdecimal_delete_E(v)+"";
			double timeLow = getMaxTimeStamp("agg_index_v2");
			String sdEndTime = getUnixTransferTime(timeLow);

			String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
			//如果出现tmptable里数据数目大于7为10，则有可能调用searchByTimeRange出问题，查询出的问题时hits长度过大，原因与
			//某个mintime太小相关
			SearchHit[] hits = searchByTimeRange("gt",mintime,"lte", maxtime,"agg_index_v2");

			// tmpTimeTable_insert(hits) errors ,java.lang.NumberFormatException: For input string: "2020-11-13T06:51:00.035Z"
			tmpTimeTable_insert(hits); // 成功则返回failure

			Thread.sleep(30000*1);
			double uptime = getMinTimeStamp("tmptime_index","timestamp");
			System.out.println("print start in inputRecord_update2");
			System.out.println("!!!!!!!!!!!!!!startTime in timetable_index:  ");
			System.out.println(getMinTimeStamp("tmptime_index", "timestamp"));
			System.out.println(uptime);
			System.out.println("print over in inputRecord_update2");
//			double lowtime = getMaxTimeStamp("agg_index_v2");
			timeTableInsert(uptime,timeLow,inputCnt,"timetable_index");
			filterTableInsert(hits,inputCnt,uptime,timeLow,sdEndTime);

//			//在input_tmp和getMinTimeStamp("tmptime_index","timestamp")之间，要注意 不加睡眠函数会出现 uptime值为Infinity的情况 导致 存入timetable的startime 为空
//			Thread.sleep(30000*1);

		}

	}

//改变了es字段将float改为double后仍然出错
	@Test
	void inputRecord_update2_timetable_index_v2() throws Exception {

		//inputData if success inputCnt++ else throw Exception and get the inputCnt
		long inputCnt= imp_Csv();
		System.out.println("the inputCnt is : ");
		System.out.println(inputCnt);
//		Thread.sleep(30000*1);
		//if timeTable_index exist , create timeTable_index
		if ( inputCnt==1) {
//			testCreatIndex("timetable_index"); //自动创建
			double uptime = getMinTimeStamp("agg_index_v2","@timestamp");
			double lowtime = getMaxTimeStamp("agg_index_v2");
			String sdEndTime = getUnixTransferTime(lowtime);
			String mintime = bigdecimal_delete_E(getMinTimeStamp("agg_index_v2","@timestamp"))+"";
			String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
			timeTableInsert(uptime,lowtime,inputCnt,"timetable_index_v2");
			SearchHit[] searchHits = searchByTimeRange("gte", mintime, "lte", maxtime, "agg_index_v2");
			filterTableInsert(searchHits,1,uptime,lowtime,sdEndTime);
		}else if (inputCnt > 1 ) {
			double v = searchProEndTime(inputCnt);//  问题： timetable 内inputid > 1 的doc 其startTime 为 null
			String mintime = bigdecimal_delete_E(v)+"";
			double timeLow = getMaxTimeStamp("agg_index_v2");
			String sdEndTime = getUnixTransferTime(timeLow);
			String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
			SearchHit[] hits = searchByTimeRange("gt",mintime,"lte", maxtime,"agg_index_v2");

			// tmpTimeTable_insert(hits) errors ,java.lang.NumberFormatException: For input string: "2020-11-13T06:51:00.035Z"
			tmpTimeTable_insert(hits);

			double uptime = getMinTimeStamp("tmptime_index","timestamp");
			System.out.println("!!!!!!!!!!!!!!startTime in timetable_index:  ");
			System.out.println(getMinTimeStamp("tmptime_index", "timestamp"));
			System.out.println(uptime);
//			double lowtime = getMaxTimeStamp("agg_index_v2");
			timeTableInsert(uptime,timeLow,inputCnt,"timetable_index_v2");
			filterTableInsert(hits,inputCnt,uptime,timeLow,sdEndTime);
		}


	}

    @Test
	void timetableinsert_solvestarttimeNull() throws Exception {
		double uptime = getMinTimeStamp("tmptime_index","timestamp");
		double timeLow = getMaxTimeStamp("agg_index_v2");
		timeTableInsert(uptime,timeLow,10000,"timetable_index_v2");
	}




	//将Unix时间戳转化成 "yyyy-MM-dd HH:mm:ss"
	@Test
	String getUnixTransferTime(double s) {             //某个时间戳;
		long time = new Double(s).longValue();
		Date date = new Date(time );
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowDateString = format.format(date);
		return  nowDateString;
	}




//	######################################-Test-####################################################################################################

//ElasticsearchStatusException[Elasticsearch exception [type=search_phase_execution_exception, reason=all shards failed]
	@Test
	void test_searchByTimeRange() throws Exception {

//	String mintime = BigDecimal.valueOf(getMinTimeStamp("agg_index_v2","@timestamp"))+"";
//
//	String maxtime = BigDecimal.valueOf(getMaxTimeStamp("agg_index_v2"))+"";


//	SearchHit[] searchHits = searchByTimeRange("gte",mintime, "lte",maxtime, "agg_index_v2");
		SearchHit[] searchHits = searchByTimeRange("gte","1608620615880", "lte","1608620615883", "agg_index_v2");
		System.out.println(searchHits);
		System.out.println(searchHits.length);

	}

//question filter no data
//@Test
//	void test_filter() throws Exception {
//	double uptime = getMinTimeStamp("agg_index_v2");
//	double lowtime = getMaxTimeStamp("agg_index_v2");
//	String mintime = BigDecimal.valueOf(getMinTimeStamp("agg_index_v2"))+"";
//	String maxtime = BigDecimal.valueOf(getMaxTimeStamp("agg_index_v2"))+"";
//	SearchHit[] searchHits = searchByTimeRange("gte", mintime, "lte", maxtime, "agg_index_v2");
//
//	filterTableInsert(searchHits,2,uptime,lowtime);
//
//}

//new filterindex build -> String timestamp
//	finished test ,change the starttime format
	@Test
	void test_filterInsert() throws Exception {

	double uptime = getMinTimeStamp("agg_index_v2","@timestamp");
	double lowtime = getMaxTimeStamp("agg_index_v2");
	String mintime = bigdecimal_delete_E(getMinTimeStamp("agg_index_v2","@timestamp"))+"";
	String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
	SearchHit[] searchHits = searchByTimeRange("gte", mintime, "lte", maxtime, "agg_index_v2");


	ArrayList<FilterObj> filterList = new ArrayList<>();


	for (SearchHit hit : searchHits) {

		String sampler_label = hit.getSourceAsMap().get("sampler_label").toString();
		long count = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_count").toString());
		long ave = Long.parseLong(String.valueOf(hit.getSourceAsMap().get("average")));
		long median= Long.parseLong(hit.getSourceAsMap().get("aggregate_report_median").toString());
		long p = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_90%_line").toString());
		long min = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_min").toString());
		long max = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_max").toString());
		long error = Long.parseLong(hit.getSourceAsMap().get("aggregate_report_error%").toString());
		double aggregate_report_rate = Double.parseDouble(String.valueOf(hit.getSourceAsMap().get("aggregate_report_rate")));
		double aggregate_report_bandwidth = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_bandwidth"))));
		double aggregate_report_stddev = Double.parseDouble(String.valueOf(String.valueOf(hit.getSourceAsMap().get("aggregate_report_stddev"))));
		System.out.println("#####################dadadada###############");
		String timestamp = hit.getSourceAsMap().get("@timestamp").toString();
		System.out.println("timestamp"+timestamp);
		System.out.println("#######################lalalala############");
		FilterObj filterObj =new FilterObj(sampler_label,count,ave,median,p,min,max,error,aggregate_report_rate,aggregate_report_bandwidth,aggregate_report_stddev,timestamp,uptime,lowtime,100,"2020-12-01");
		filterList.add(filterObj);
	}



	//BulkRequest 批量导入
	BulkRequest bulkRequest = new BulkRequest();
	bulkRequest.timeout("10s");
	for (int i =0; i<filterList.size();i++) {
		//批量更新和批量删除就在这里修改对应的请求就行
		bulkRequest.add(
				new IndexRequest("filter_index")
						.id(""+(i+1))
						.source(JSON.toJSONString(filterList.get(i)),XContentType.JSON));
	}
	BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
	if (!bulkResponse.hasFailures()) {
		System.out.println("save in filterTable success");
	}else {
		System.out.println("save filterTable failure");
	}
}

	@Test
	void getMinTimeStamp_test() throws Exception {
		System.out.println("minTime in tmptime_index");
		System.out.println(getMinTimeStamp("tmptime_index", "timestamp"));
		System.out.println("maxtime in agg_index-v2");
		System.out.println(getMaxTimeStamp("agg_index_v2"));
	}




	@Test
	void unixTimeFormatTest() throws Exception {
		double lowtime = getMaxTimeStamp("agg_index_v2");
		System.out.println(getUnixTransferTime(lowtime));

	}


/**
 * 1.偶尔 tmptime_index 插入10条而非7条？
 * 2.在  timetable_index startTimeStamp 字段为空 debug 发现是getMinTimeStamp 问题
 * 3.问题排查
 */


//测试 根据时间段取记录，取出数据的条数
	@Test
	void testDataNUmbererror_in_tmptime_index() throws Exception {


		long inputCnt;
		inputCnt = 20;
		//searchProEndTime(inputCnt) 只支持inputCnt 为8 而不支持大于8的参数，因为此时输出为0.0
		double v = searchProEndTime(inputCnt);//  问题： timetable 内inputid > 1 的doc 其startTime 为 null

		System.out.println("searchProEndTime: "+v);
		String mintime = bigdecimal_delete_E(v)+"";
		double timeLow = getMaxTimeStamp("agg_index_v2");
		String sdEndTime = getUnixTransferTime(timeLow);
		String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
		SearchHit[] hits = searchByTimeRange("gt",mintime,"lte", maxtime,"agg_index_v2");
		System.out.println("hits numbers"+hits.length);
		System.out.println("num"+numOfDoc("agg_index_v2"));
//		// tmpTimeTable_insert(hits) errors ,java.lang.NumberFormatException: For input string: "2020-11-13T06:51:00.035Z"
//		tmpTimeTable_insert(hits);
//
//		double uptime = getMinTimeStamp("tmptime_index","timestamp");
//		System.out.println("!!!!!!!!!!!!!!startTime in timetable_index:  ");
//		System.out.println(getMinTimeStamp("tmptime_index", "timestamp"));
//		System.out.println(uptime);
////			double lowtime = getMaxTimeStamp("agg_index_v2");
//		timeTableInsert(uptime,timeLow,inputCnt,"timetable_index_v2");
//		filterTableInsert(hits,inputCnt,uptime,timeLow,sdEndTime);

}


	@Test
	void testGetProTime() throws IOException {
		long num = numOfDoc("agg_index_v2");
		System.out.println("docNum: "+num);
		for (long i = 2; i <= num/7; i++) {  //success!!!(inputid 7.8 proendtime= 0.0 reason: ProEndTime() 中的函数的索引参数是timetable_index_v2)
			System.out.println("docnum:"+i+" protime"+test_searchProEndTime(i));
		}
//		System.out.println("Doc 7's Protime:"+test_searchProEndTime(7));
	}

	@Test
	double test_searchProEndTime(long inputId) throws IOException { //success

		SearchRequest searchRequest = new SearchRequest("timetable_index");

		//构建搜索条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //设计模式 builder

		//查询条件，使用QueryBuilders 工具来实现  termQueryBuilder 精确查询
		//QueryBuilders.matchAllQuery()
		//inputCnt-1;
		String num = String.valueOf(inputId-1);
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("inputId", num);
		searchSourceBuilder.query(termQueryBuilder);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		double res =0.0;
		for (SearchHit documentFields : searchResponse.getHits().getHits()) {
			res = Double.parseDouble(documentFields.getSourceAsMap().get("endTimeStamp").toString());
		}
		return res;
	}

	@Test
	void uptime_test() throws Exception {
		double uptime = getMinTimeStamp("tmptime_index","timestamp");
		System.out.println(uptime);
	}







}
