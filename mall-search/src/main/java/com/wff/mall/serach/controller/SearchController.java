package com.wff.mall.serach.controller;

import com.wff.mall.serach.service.MallSearchService;
import com.wff.mall.serach.vo.SearchParam;
import com.wff.mall.serach.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/18 22:48
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model) {

        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}
