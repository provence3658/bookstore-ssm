package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.vo.CartVo;

public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer bookId, Integer count);
    ServerResponse<CartVo> update(Integer userId, Integer bookId, Integer count);
    ServerResponse<CartVo> delete(Integer userId, String bookIds);
    ServerResponse<CartVo> list(Integer userId);
    ServerResponse<CartVo> selectOrUnselectAll(Integer userId, Integer checked, Integer bookId);
    ServerResponse<Integer> getCartCount(Integer userId);
}
