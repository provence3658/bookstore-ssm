package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);
    ServerResponse updateCategoryName(Integer categoryId, String categoryName);
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);
    ServerResponse getCategoryNameById(Integer categoryId);
    ServerResponse getParentId(Integer categoryId);
}
