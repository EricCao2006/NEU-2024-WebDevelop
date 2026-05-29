package com.ec.onlinestore.controller;

import com.ec.onlinestore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        return userService.login(username, password);
    }

    @PutMapping("/{id}/address")
    public Map<String, Object> updateAddress(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        boolean success = userService.updateAddress(id, body.get("address"));
        return Map.of("success", success);
    }
}