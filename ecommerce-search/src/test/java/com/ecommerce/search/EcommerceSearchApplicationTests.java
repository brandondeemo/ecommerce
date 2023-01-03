package com.ecommerce.search;

import com.alibaba.fastjson.JSON;
import com.ecommerce.search.config.ElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class EcommerceSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @ToString
    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Test
    public void searchData() throws IOException {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定DSL 检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 1.1 构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        // 1.2 构造聚合 aggregate 条件
        // a. 按照年龄得值进行聚合
        TermsAggregationBuilder aggAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(aggAgg);
        // b. 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件" + searchSourceBuilder.toString());

        searchRequest.source(searchSourceBuilder);
        // 2. 执行检索
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

        // 3. 分析结果
        System.out.println(searchResponse.toString());
        Map map = JSON.parseObject(searchResponse.toString(), Map.class);

        // 3.1 获取所有结果数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit searchHit : searchHits) {
             /*
             元数据：
             "_index" : "bank",
            "_type" : "account",
            "_id" : "970",
            "_score" : 5.4032025,
              */
            String str = searchHit.getSourceAsString();
            Account account = JSON.parseObject(str, Account.class);

            System.out.println("account" + account);
        }

        // 3.2 获取这次检索到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄： " + keyAsString + "->" + bucket.getDocCount());
        }

        Avg balanceAvgAgg = aggregations.get("balanceAvg");
        System.out.println("平均薪资： " + balanceAvgAgg.getValue());
    }

    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
//        indexRequest.source("userName", "zhangsan", "age", 18, "gender", "M");
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(18);
        user.setGender("M");

        String jsonString = JSON.toJSONString(user);

        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        // 提取有用的相应数据
        System.out.println(index);

    }


    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;

        public User(String userName, String gender, int age) {
            this.userName = userName;
            this.gender = gender;
            this.age = age;
        }

        public User() {

        }
    }


    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
