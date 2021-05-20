package com.wff.mall.serach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wff.mall.serach.config.MallElasticsearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class MallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

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
        //建立serch请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");

        //设置检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg")
                .field("age")
                .size(10)
                .subAggregation(AggregationBuilders
                        .avg("ageAvg")
                        .field("age"));
        searchSourceBuilder.aggregation(ageAgg);

        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        System.out.println(searchSourceBuilder.toString());

        searchRequest.source(searchSourceBuilder);

        //开始请求
        SearchResponse searchResponse = client.search(searchRequest, MallElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse.toString());
//        ObjectMapper mapper = new ObjectMapper();
//        Map map = mapper.readValue(searchResponse.toString(), Map.class);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        ObjectMapper mapper = new ObjectMapper();
        for (SearchHit hit : searchHits) {
            String s = hit.getSourceAsString();
            Account account = mapper.readValue(s, Account.class);
            System.out.println("account = " + account);
        }

        Aggregations aggregations = searchResponse.getAggregations();
        Terms terms = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄" + keyAsString + "共" + bucket.getDocCount());
        }
        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资" + balanceAvg1.getValue());
    }

    @Test
    public void testIndexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUsername("张三");
        user.setAge(20);
        user.setGender("男");
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse index = client.index(indexRequest, MallElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User {
        private String username;
        private String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
