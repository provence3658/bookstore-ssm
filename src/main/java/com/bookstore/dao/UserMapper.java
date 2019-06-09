package com.bookstore.dao;

import com.bookstore.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(@Param("username") String username);

    User login(@Param("username")String username, @Param("password")String password);

    int checkPassword(@Param("password")String password, @Param("userId")Integer userId);

    int resetPassword(@Param("passwordNew")String passwordNew, @Param("username")String username);

    List<User> listAllUser();

    int getUserNumber();
}