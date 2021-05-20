package com.wff.mall.search.service;

import com.wff.mall.search.vo.SearchParam;
import com.wff.mall.search.vo.SearchResult;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/19 20:29
 */
public interface MallSearchService {
    /**
     * 检索方法
     * @param param 检索的所有参数
     * @return 检索的结果
     */
    SearchResult search(SearchParam param);
}
