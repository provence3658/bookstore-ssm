package com.bookstore.controller.client;

import com.bookstore.common.ServerResponse;
import com.bookstore.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/category/")
public class CategoryController {
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        return iCategoryService.getChildrenParallelCategory(categoryId);
    }

    @RequestMapping("get_name.do")
    @ResponseBody
    public ServerResponse getCNameById(Integer categoryId) {
        return iCategoryService.getCategoryNameById(categoryId);
    }
}
