package com.ec.onlinestore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ec.onlinestore.entity.User;
import com.ec.onlinestore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public Map<String, Object> login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).eq("password", password);
        User user = userMapper.selectOne(wrapper);

        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("success", true);
            result.put("role", user.getRole());
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("address", user.getAddress());
        } else {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }

    public boolean updateAddress(Integer userId, String address) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setAddress(address);
            return userMapper.updateById(user) > 0;
        }
        return false;
    }
}