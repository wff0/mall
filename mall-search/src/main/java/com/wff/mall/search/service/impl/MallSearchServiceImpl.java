package com.wff.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wff.common.to.es.SkuEsModel;
import com.wff.common.utils.R;
import com.wff.mall.search.config.MallElasticsearchConfig;
import com.wff.mall.search.constant.EsConstant;
import com.wff.mall.search.feign.ProductFeignService;
import com.wff.mall.search.service.MallSearchService;
import com.wff.mall.search.vo.AttrResponseVo;
import com.wff.mall.search.vo.BrandVo;
import com.wff.mall.search.vo.SearchParam;
import com.wff.mall.search.vo.SearchResult;
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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        SearchResult searchResult = null;
        //1?????????????????????
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //2?????????????????????
            SearchResponse response = client.search(searchRequest, MallElasticsearchConfig.COMMON_OPTIONS);

            searchResult = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam Param) {
        // ???????????????DSL?????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1. ???????????? ??????(??????????????????????????????????????????????????????) ?????????????????????Query
        // 1.1 must
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.hasLength(Param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", Param.getKeyword()));
        }
        // 1.2 bool - filter Catalog3Id
        if (Param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", Param.getCatalog3Id()));
        }
        // 1.2 bool - brandId [??????]
        if (Param.getBrandId() != null && Param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", Param.getBrandId()));
        }
        // ????????????
        if (Param.getAttrs() != null && Param.getAttrs().size() > 0) {

            for (String attrStr : Param.getAttrs()) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                // ?????????id  ?????????????????????
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                // ?????????????????????Query ???????????????????????????????????? nested ??????
                NestedQueryBuilder attrsQuery = QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None);
                boolQuery.filter(attrsQuery);
            }
        }
        // 1.2 bool - filter [??????]
        if (Param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", Param.getHasStock() == 1));
        }
        // 1.2 bool - filter [????????????]
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

        // ??????????????????????????????????????????
        sourceBuilder.query(boolQuery);

        // 1.??????
        if (StringUtils.hasLength(Param.getSort())) {
            String[] s = Param.getSort().split("_");
            sourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }
        // 2.?????? pageSize ??? 5
        sourceBuilder.from((Param.getPageNum() - 1) * EsConstant.PRODUCT_PASIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PASIZE);

        // 3.??????
        if (StringUtils.hasLength(Param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }
        // ????????????
        // TODO 1.????????????
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg");
        brandAgg.field("brandId").size(50);
        // ????????????????????????
        brandAgg.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImgAgg").field("brandImg").size(1));
        // ????????????????????? sourceBuilder
        sourceBuilder.aggregation(brandAgg);
        // TODO 2.????????????
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg");
        catalogAgg.field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogNameAgg").field("catalogName").size(1));
        // ????????????????????? sourceBuilder
        sourceBuilder.aggregation(catalogAgg);
        // TODO 3.???????????? attr_agg ?????????????????????
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attrAgg", "attrs");
        // 3.1 ????????????????????????attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg");
        attrIdAgg.field("attrs.attrId").size(10);
        // 3.1.1 ?????????????????????attrId?????????attrName
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1));
        // 3.1.2 ?????????????????????attrId?????????????????????????????????attrValue	???????????????????????????????????? ?????????50
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(50));
        // 3.2 ???????????????????????????????????????
        attrAgg.subAggregation(attrIdAgg);
        // ????????????????????? sourceBuilder
        sourceBuilder.aggregation(attrAgg);
        log.info("\n???????????????->\n" + sourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * ??????????????????
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        // 1.?????????????????????????????????
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        if (!ObjectUtils.isEmpty(hits.getHits())) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = new SkuEsModel();
                skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.hasLength(param.getKeyword())) {
                    // 1.1 ???????????????????????????
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highlightFields = skuTitle.getFragments()[0].string();
                    // 1.2 ??????????????????
                    skuEsModel.setSkuTitle(highlightFields);
                }
                skuEsModels.add(skuEsModel);
            }
        }
        result.setProducts(skuEsModels);

        // 2.????????????????????????????????????????????????
        ArrayList<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 2.1 ???????????????id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            // 2.2 ?????????????????????
            String attrNameAgg = ((ParsedStringTerms) bucket.getAggregations().get("attrNameAgg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrNameAgg);
            // 2.3 ????????????????????????
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

        // 3.????????????????????????????????????????????????
        ArrayList<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brandAgg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 3.1 ???????????????id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            // 3.2 ??????????????????
            String brandNameAgg = ((ParsedStringTerms) bucket.getAggregations().get("brandNameAgg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandNameAgg);
            // 3.3 ?????????????????????
            String brandImgAgg = ((ParsedStringTerms) (bucket.getAggregations().get("brandImgAgg"))).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImgAgg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4.??????????????????????????????????????????
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalogAgg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //4.1 ????????????id
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            //4.2 ???????????????
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalogNameAgg");
            catalogVo.setCatalogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // ================????????????????????????????????????
        // 5.????????????-??????
        result.setPageNum(param.getPageNum());
        // ????????????
        result.setTotal(hits.getTotalHits().value);
        // ????????????????????????
        result.setTotalPages((int) (result.getTotal() + EsConstant.PRODUCT_PASIZE - 1) / EsConstant.PRODUCT_PASIZE);
        // ???????????????
        ArrayList<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= result.getTotalPages(); i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6.???????????????????????????
        if (!ObjectUtils.isEmpty(param.getAttrs())) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.getAttrsInfo(Long.parseLong(s[0]));
                // ??????????????????????????????????????? ????????????????????????
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setName(data.getAttrName());
                } else {
                    // ???????????????id????????????
                    navVo.setName(s[0]);
                }
                // ???????????????????????? ??????????????????
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.mall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }
        // ???????????????
        if (!ObjectUtils.isEmpty(param.getBrandId())) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setName("??????");
            // TODO ????????????????????????
            R r = productFeignService.brandInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                // ??????????????????ID
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getName()).append(";");
                    replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.mall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }

        return result;
    }

    /**
     * ????????????
     * key ??????????????????key
     */
    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            // ??????????????????????????????java????????????
            encode = encode.replace("+", "%20");
            encode = encode.replace("%28", "(").replace("%29", ")");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
