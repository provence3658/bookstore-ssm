package com.bookstore.dao;

import com.bookstore.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdBookId(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    List<Cart> selectCartByUserId(@Param("userId")Integer userId);

    int selectCartBookCheckedStatusByUserId(@Param("userId")Integer userId);

    int deleteByUserIdBookIds(@Param("userId") Integer userId, @Param("bookIdList") List<String> bookIdList);

    int checkedOrUncheckedBook(@Param("userId") Integer userId, @Param("checked") Integer checked, @Param("bookId") Integer bookId);

    int getCartCount(@Param("userId") Integer userId);

    List<Cart> selectCheckedCartByUserId(@Param("userId") Integer userId);
}