package com.ec.onlinestore.controller;

import com.ec.onlinestore.entity.Order;
import com.ec.onlinestore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Order order) {
        boolean success = orderService.createOrder(order);
        return Map.of("success", success, "message", success ? "下单成功" : "下单失败");
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Integer userId) {
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        boolean success = orderService.updateOrderStatus(id, body.get("status"));
        return Map.of("success", success);
    }
}