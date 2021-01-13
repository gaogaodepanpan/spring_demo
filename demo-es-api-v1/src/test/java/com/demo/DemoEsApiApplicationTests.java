package com.demo;


import com.alibaba.fastjson.JSON;
import com.demo.pojo.*;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
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
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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


	//注意此处的time_filed_name 的选择会有不同，agg_index_v2 时，time_filed_name is @timestamp
	//对于其他的index时，其time_filed_name 可能不是@timestamp
	double tmpMinTimestamp;
	@Test
	double getMinTimeStamp(String idx,String time_filed_name) throws Exception {
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
				e.printStackTrace();
			}
			long time = d.getTime();

			System.out.println(time+"run in tmpTimeTable_insert()");


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


	@Test
	void calById() throws Exception {
		calDiffertest(3,4);

	}


//	String idx,String config_path ,String config_name
    @Test
	boolean imp_Csv( ) throws Exception {

		boolean input_status;

		//save data in "agg_index_v2" ,conf in logstash_default.conf
		Runtime run = Runtime.getRuntime();
		try {

		//"agg_index"

			long tmpCnt = numOfDoc("agg_index_v2");

			Thread.sleep(20000 );

			//input
			run.exec("cmd /k start C:\\Users\\I524987\\Desktop\\dashboard\\logstash-7.6.2\\bin\\logstash -f C:\\Users\\I524987\\Desktop\\dashboard\\logstash-7.6.2\\bin\\logstash_default.conf");

//			run.exec("cmd /k start "+config_path+"\\logstash -f "+config_path+"\\"+config_name);
			//4min could support 0.2 millis data;
			Thread.sleep(60000 );



			long docNum = numOfDoc("agg_index_v2");


			if (docNum  - tmpCnt > 0 ) {
				input_status = true;

			}else{
				input_status = false;

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("inputData error");
		}


		return input_status;

	}




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

	//将Unix时间戳转化成 "yyyy-MM-dd HH:mm:ss"
	@Test
	String getUnixTransferTime(double s) {             //某个时间戳;
		long time = new Double(s).longValue();
		Date date = new Date(time );
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowDateString = format.format(date);
		return  nowDateString;
	}



	//休眠方式解决查询插入冲突
	@Test
	void inputRecord_update3() throws Exception { //sleep 方式
		long inputCnt =0 ;
		//inputData if success inputCnt++ else throw Exception and get the inputCnt
		boolean inputstatus= imp_Csv();
//		Thread.sleep(30000*1);
		//if timeTable_index exist , create timeTable_index
		if (!testExistIndex("timetable_index") && inputstatus) {
			testCreatIndex("timetable_index"); //自动创建
			double uptime = getMinTimeStamp("agg_index_v2","@timestamp");
			double lowtime = getMaxTimeStamp("agg_index_v2");
			String sdEndTime = getUnixTransferTime(lowtime);
			String mintime = bigdecimal_delete_E(getMinTimeStamp("agg_index_v2","@timestamp"))+"";
			String maxtime = bigdecimal_delete_E(getMaxTimeStamp("agg_index_v2"))+"";
			timeTableInsert(uptime,lowtime,1,"timetable_index");
			SearchHit[] searchHits = searchByTimeRange("gte", mintime, "lte", maxtime, "agg_index_v2");
			filterTableInsert(searchHits,1,uptime,lowtime,sdEndTime);
		}else if (testExistIndex("timetable_index") && inputstatus ) {
			long inputId_timetable_cur = numOfDoc("timetable_index");
			inputCnt = inputId_timetable_cur+1;
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

			Thread.sleep(30000);
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


}
