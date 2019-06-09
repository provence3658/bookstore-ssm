package com.bookstore.service;

import com.bookstore.common.ServerResponse;
import com.bookstore.pojo.User;
import com.github.pagehelper.PageInfo;

public interface IUserService {

    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid(String str);
    ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew);
    ServerResponse<User> getInformation(Integer userId);
    ServerResponse<User> updateInformation(User user);
    ServerResponse checkAdminRole(User user);
    ServerResponse<PageInfo> listAllUser(Integer pageNum, Integer pageSize);
    ServerResponse<Integer> getUserNumber();
}
