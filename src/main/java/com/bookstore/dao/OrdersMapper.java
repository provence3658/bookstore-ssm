package com.bookstore.dao;

import com.bookstore.pojo.Orders;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrdersMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Orders record);

    int insertSelective(Orders record);

    Orders selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Orders record);

    int updateByPrimaryKey(Orders record);

    Orders selectByUserIdAndOrderNo(@Param("userId")Integer userId, @Param("orderNo")Long orderNo);

    Orders selectByOrderNo(Long orderNo);

    List<Orders> selectByUserId(@Param("userId")Integer userId);

    List<Orders> selectAll();

    Integer getOrderNumber();
}