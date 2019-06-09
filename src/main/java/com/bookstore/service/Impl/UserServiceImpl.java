package com.bookstore.service.Impl;

import com.bookstore.common.Const;
import com.bookstore.common.ServerResponse;
import com.bookstore.dao.UserMapper;
import com.bookstore.pojo.User;
import com.bookstore.service.IUserService;
import com.bookstore.util.MD5;
import com.bookstore.vo.UserVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int rowCount = userMapper.checkUsername(username);
        if (rowCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //MD5加密
        String md5Password = MD5.MD5EncodeUtf8(password);
        User user = userMapper.login(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = this.checkValid(user.getUsername());
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5.MD5EncodeUtf8(user.getPassword()));

        int rowCount = userMapper.insert(user);
        if (rowCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str) {
        int rowCount = userMapper.checkUsername(str);
        if (rowCount > 0) {
            return ServerResponse.createByErrorMessage("用户名已存在");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        String md5Password = MD5.MD5EncodeUtf8(passwordOld);
        int rowCount = userMapper.checkPassword(md5Password, user.getId());
        if (rowCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        String md5PassNew = MD5.MD5EncodeUtf8(passwordNew);
        user.setPassword(md5PassNew);
        int count = userMapper.updateByPrimaryKeySelective(user);
        if (count > 0) {
            return  ServerResponse.createBySuccessMessage("修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    public ServerResponse<User> updateInformation(User user) {
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUsername(user.getUsername());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    public ServerResponse<PageInfo> listAllUser(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> userList = userMapper.listAllUser();
        List<UserVo> UserVoList = this.assembleUserVoList(userList);
        PageInfo pageInfo = new PageInfo(userList);
        pageInfo.setList(UserVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
    private List<UserVo> assembleUserVoList(List<User> userList){
        List<UserVo> userVoList = Lists.newArrayList();
        for (User user : userList) {
            UserVo userVo = new UserVo();
            userVo.setId(user.getId());
            userVo.setUsername(user.getUsername());
            userVo.setEmail(user.getEmail());
            userVo.setPhone(user.getPhone());
            userVoList.add(userVo);
        }
        return userVoList;
    }
    public ServerResponse<Integer> getUserNumber() {
        Integer number = userMapper.getUserNumber();
        return ServerResponse.createBySuccess(number);
    }
}
