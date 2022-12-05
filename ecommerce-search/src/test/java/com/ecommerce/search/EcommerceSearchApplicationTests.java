package com.ecommerce.search;

import com.alibaba.fastjson.JSON;
import com.ecommerce.search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
class EcommerceSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
//        indexRequest.source("userName", "zhangsan", "age", 18, "gender", "M");
        User user = new User();
        String jsonString = JSON.toJSONString(user);

        indexRequest.source(jsonString, XContentType.JSON);


        try {
            // 执行操作
            IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

            // 提取有用的相应数据
            System.out.println(index);
        } catch (IOException e) {

        }

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
