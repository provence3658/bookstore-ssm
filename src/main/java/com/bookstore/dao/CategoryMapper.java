package com.bookstore.dao;

import com.bookstore.pojo.Category;

import java.util.List;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    List<Category> selectCategoryChildByParentId(Integer parentId);

    int selectParentId(Integer categoryId);

    String getCategoryNameById(Integer categoryId);
}