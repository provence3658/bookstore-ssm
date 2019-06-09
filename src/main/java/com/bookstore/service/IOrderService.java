package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.vo.OrdersVo;
import com.github.pagehelper.PageInfo;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Long orderNo, Integer userId, String path);
    ServerResponse alipayCallback(Map<String, String> params);
    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
    ServerResponse create(Integer userId, Integer shippingId);
    ServerResponse<String> cancel(Integer userId, Long orderNo);
    ServerResponse<String> confirm(Integer userId, Long orderNo);
    ServerResponse getOrderCartBook(Integer userId);
    ServerResponse<OrdersVo> getOrderDetail(Integer userId, Long orderNo);
    ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize);
    ServerResponse<OrdersVo> manageDetail(Long orderNo);
    ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);
    ServerResponse<String> manageSend(Long orderNo);
    ServerResponse<Integer> getOrderNumber();
}
