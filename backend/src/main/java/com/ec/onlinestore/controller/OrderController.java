package com.ec.onlinestore.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ec.onlinestore.entity.Order;
import com.ec.onlinestore.entity.Product;
import com.ec.onlinestore.entity.User;
import java.util.stream.Collectors;

import com.ec.onlinestore.mapper.ProductMapper;
import com.ec.onlinestore.mapper.UserMapper;
import com.ec.onlinestore.mapper.OrderMapper;
import com.ec.onlinestore.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    private final OrderMapper orderMapper;

    private final ProductMapper productMapper;

    private final UserMapper userMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper, ProductMapper productMapper, UserMapper userMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Order order) {
        if (order.getSkuName() == null || order.getSkuName().isEmpty()) {
            order.setSkuName("标准版");
        }
        if (order.getOrderStatus() == null) {
            order.setOrderStatus("waiting_ship");
        }
        if (order.getOrderTime() == null) {
            order.setOrderTime(LocalDateTime.now());
        }
        boolean success = orderService.createOrder(order);
        return Map.of("success", success, "message", success ? "下单成功" : "下单失败");
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getOrdersByUser(@PathVariable Integer userId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("order_time");
        List<Order> orders = orderMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("productId", order.getProductId());

            Product product = productMapper.selectById(order.getProductId());
            item.put("productName", product != null ? product.getName() : "商品已下架");
            item.put("skuName", order.getSkuName() != null ? order.getSkuName() : "默认款式");
            item.put("quantity", order.getQuantity());
            item.put("totalPrice", order.getTotalPrice());
            item.put("orderStatus", order.getOrderStatus());
            item.put("orderTime", order.getOrderTime());
            result.add(item);
        }
        return result;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // 申请退款/退货
    @PostMapping("/{id}/refund")
    public Map<String, Object> applyRefund(@PathVariable Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Map.of("success", false, "message", "订单不存在");
        }
        if ("refunded".equals(order.getOrderStatus())) {
            return Map.of("success", false, "message", "订单已退款");
        }
        if ("refunding".equals(order.getOrderStatus())) {
            return Map.of("success", false, "message", "退款申请已在处理中");
        }
        order.setOrderStatus("refunding");
        int rows = orderMapper.updateById(order);
        return Map.of("success", rows > 0, "message", rows > 0 ? "退款申请已提交" : "操作失败");
    }

    // 获取商家的订单（包含自己商品的订单）
    @GetMapping("/merchant/{userId}")
    public List<Map<String, Object>> getMerchantOrders(@PathVariable Integer userId) {
        // 获取商家的所有商品ID
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("user_id", userId);
        List<Product> products = productMapper.selectList(productWrapper);
        List<Integer> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询包含这些商品的订单
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.in("product_id", productIds);
        orderWrapper.orderByDesc("order_time");
        List<Order> orders = orderMapper.selectList(orderWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());

            Product product = productMapper.selectById(order.getProductId());
            item.put("productName", product != null ? product.getName() : "商品已下架");
            item.put("skuName", order.getSkuName());
            item.put("quantity", order.getQuantity());
            item.put("totalPrice", order.getTotalPrice());
            item.put("orderStatus", order.getOrderStatus());
            item.put("orderTime", order.getOrderTime());

            // 获取买家信息
            User buyer = userMapper.selectById(order.getUserId());
            if (buyer != null) {
                item.put("address", buyer.getAddress());
                item.put("buyerName", buyer.getUsername());
                item.put("buyerPhone", buyer.getPhone());
            }

            result.add(item);
        }
        return result;
    }

    // 更新订单状态
    @PutMapping("/{id}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String orderStatus = body.get("orderStatus");
        Order order = orderMapper.selectById(id);
        if (order != null) {
            order.setOrderStatus(orderStatus);
            int rows = orderMapper.updateById(order);
            return Map.of("success", rows > 0);
        }
        return Map.of("success", false);
    }
}