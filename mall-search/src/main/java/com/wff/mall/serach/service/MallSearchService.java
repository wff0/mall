package com.wff.mall.serach.service;

import com.wff.mall.serach.vo.SearchParam;
import com.wff.mall.serach.vo.SearchResult;

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
