package com.ec.onlinestore.controller;

import com.ec.onlinestore.entity.User;
import com.ec.onlinestore.mapper.UserMapper;
import com.ec.onlinestore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;  // 注册功能需要

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String phone, @RequestParam String password) {
        return userService.login(phone, password);
    }

    @PutMapping("/{id}/address")
    public Map<String, Object> updateAddress(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        boolean success = userService.updateAddress(id, body.get("address"));
        return Map.of("success", success);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        // 检查手机号是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", user.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            return Map.of("success", false, "message", "手机号已注册");
        }
        user.setRole("user");
        int rows = userMapper.insert(user);
        return Map.of("success", rows > 0);
    }

    // 修改用户名
    @PutMapping("/{id}/username")
    public Map<String, Object> updateUsername(@PathVariable Integer id, @RequestParam String username) {
        User user = userMapper.selectById(id);
        user.setUsername(username);
        int rows = userMapper.updateById(user);
        return Map.of("success", rows > 0);
    }

    // 修改手机号
    @PutMapping("/{id}/phone")
    public Map<String, Object> updatePhone(@PathVariable Integer id, @RequestParam String phone) {
        // 检查手机号是否已被其他用户使用
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone).ne("id", id);
        if (userMapper.selectCount(wrapper) > 0) {
            return Map.of("success", false, "message", "手机号已被注册");
        }
        User user = userMapper.selectById(id);
        user.setPhone(phone);
        int rows = userMapper.updateById(user);
        return Map.of("success", rows > 0);
    }

    // 修改密码
    @PutMapping("/{id}/password")
    public Map<String, Object> updatePassword(@PathVariable Integer id, @RequestParam String password) {
        User user = userMapper.selectById(id);
        user.setPassword(password);
        int rows = userMapper.updateById(user);
        return Map.of("success", rows > 0);
    }
}