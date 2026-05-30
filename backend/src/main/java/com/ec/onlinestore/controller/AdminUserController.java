package com.ec.onlinestore.controller;

import com.ec.onlinestore.entity.User;
import com.ec.onlinestore.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final UserMapper userMapper;

    public AdminUserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // 获取所有用户
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    // 修改用户角色
    @PutMapping("/users/{id}/role")
    public Map<String, Object> updateRole(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setRole(body.get("role"));
            userMapper.updateById(user);
            return Map.of("success", true);
        }
        return Map.of("success", false);
    }

    // 修改用户状态
    @PutMapping("/users/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(body.get("status"));
            userMapper.updateById(user);
            return Map.of("success", true);
        }
        return Map.of("success", false);
    }

    // 重置密码
    @PutMapping("/users/{id}/password")
    public Map<String, Object> resetPassword(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(body.get("password"));
            userMapper.updateById(user);
            return Map.of("success", true);
        }
        return Map.of("success", false);
    }
}