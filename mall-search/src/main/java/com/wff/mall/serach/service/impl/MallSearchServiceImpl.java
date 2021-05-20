package com.wff.mall.serach.service.impl;

import com.alibaba.fastjson.JSON;
import com.wff.common.to.es.SkuEsModel;
import com.wff.mall.serach.config.MallElasticsearchConfig;
import com.wff.mall.serach.constant.EsConstant;
import com.wff.mall.serach.service.MallSearchService;
import com.wff.mall.serach.vo.SearchParam;
import com.wff.mall.serach.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/19 20:30
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam param) {

        SearchResult searchResult = null;
        //1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //2、执行检索请求
            SearchResponse response = client.search(searchRequest, MallElasticsearchConfig.COMMON_OPTIONS);

            searchResult = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    /**
     * 构建检索请求
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam Param) {
        // 帮我们构建DSL语句的
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1. 模糊匹配 过滤(按照属性、分类、品牌、价格区间、库存) 先构建一个布尔Query
        // 1.1 must
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.hasLength(Param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", Param.getKeyword()));
        }
        // 1.2 bool - filter Catalog3Id
        if (Param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", Param.getCatalog3Id()));
        }
        // 1.2 bool - brandId [集合]
        if (Param.getBrandId() != null && Param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", Param.getBrandId()));
        }
        // 属性查询
        if (Param.getAttrs() != null && Param.getAttrs().size() > 0) {

            for (String attrStr : Param.getAttrs()) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                // 检索的id  属性检索用的值
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                // 构建一个嵌入式Query 每一个必须都得生成嵌入的 nested 查询
                NestedQueryBuilder attrsQuery = QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None);
                boolQuery.filter(attrsQuery);
            }
        }
        // 1.2 bool - filter [库存]
        if (Param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", Param.getHasStock() == 1));
        }
        // 1.2 bool - filter [价格区间]
        if (StringUtils.hasLength(Param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = Param.getSkuPrice().split("_");
            if (Param.getSkuPrice().startsWith("_")) {
                rangeQuery.lte(s[1]);
            } else if (Param.getSkuPrice().endsWith("_")) {
                rangeQuery.gte(s[0]);
            } else {
                rangeQuery.gte(s[0]).lte(s[1]);
            }
            boolQuery.filter(rangeQuery);
        }

        // 把以前所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);

        // 1.排序
        if (StringUtils.hasLength(Param.getSort())) {
            String[] s = Param.getSort().split("_");
            sourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }
        // 2.分页 pageSize ： 5
        sourceBuilder.from((Param.getPageNum() - 1) * EsConstant.PRODUCT_PASIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PASIZE);

        // 3.高亮
        if (StringUtils.hasLength(Param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }
        // 聚合分析
        // TODO 1.品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg");
        brandAgg.field("brandId").size(50);
        // 品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImgAgg").field("brandImg").size(1));
        // 将品牌聚合加入 sourceBuilder
        sourceBuilder.aggregation(brandAgg);
        // TODO 2.分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg");
        catalogAgg.field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogNameAgg").field("catalogName").size(1));
        // 将分类聚合加入 sourceBuilder
        sourceBuilder.aggregation(catalogAgg);
        // TODO 3.属性聚合 attr_agg 构建嵌入式聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attrAgg", "attrs");
        // 3.1 聚合出当前所有的attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg");
        attrIdAgg.field("attrs.attrId").size(10);
        // 3.1.1 聚合分析出当前attrId对应的attrName
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1));
        // 3.1.2 聚合分析出当前attrId对应的所有可能的属性值attrValue	这里的属性值可能会有很多 所以写50
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(50));
        // 3.2 将这个子聚合加入嵌入式聚合
        attrAgg.subAggregation(attrIdAgg);
        // 将属性聚合加入 sourceBuilder
        sourceBuilder.aggregation(attrAgg);
        log.info("\n构建语句：->\n" + sourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam Param) {
        SearchResult result = new SearchResult();
        // 1.返回的所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        if (!ObjectUtils.isEmpty(hits.getHits())) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = new SkuEsModel();
                skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                skuEsModels.add(skuEsModel);
            }
        }
        result.setProducts(skuEsModels);

        // 2.当前所有商品涉及到的所有属性信息
        ArrayList<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 2.1 得到属性的id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            // 2.2 得到属性的名字
            String attrNameAgg = ((ParsedStringTerms) bucket.getAggregations().get("attrNameAgg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrNameAgg);
            // 2.3 得到属性的所有值
            List<String> attrValueAgg = ((ParsedStringTerms) bucket.getAggregations()
                    .get("attrValueAgg"))
                    .getBuckets()
                    .stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            attrVo.setAttrValue(attrValueAgg);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 3.当前所有商品涉及到的所有品牌信息
        ArrayList<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brandAgg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 3.1 得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            // 3.2 得到品牌的名
            String brandNameAgg = ((ParsedStringTerms) bucket.getAggregations().get("brandNameAgg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandNameAgg);
            // 3.3 得到品牌的图片
            String brandImgAgg = ((ParsedStringTerms) (bucket.getAggregations().get("brandImgAgg"))).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImgAgg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4.当前商品所有涉及到的分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalogAgg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //4.1得到分类id
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            //4.2得到分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalogNameAgg");
            catalogVo.setCatalogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // ================以上信息从聚合信息中获取
        // 5.分页信息-页码
        result.setPageNum(Param.getPageNum());
        // 总记录数
        result.setTotal(hits.getTotalHits().value);
        // 总页码：计算得到
        result.setTotalPages((int) (result.getTotal() + EsConstant.PRODUCT_PASIZE - 1) / EsConstant.PRODUCT_PASIZE);
        // 设置导航页

        // 6.构建面包屑导航功能

        // 品牌、分类

        return result;
    }

}
