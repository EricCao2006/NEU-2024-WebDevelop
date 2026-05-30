package com.ec.onlinestore.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ec.onlinestore.entity.Order;
import com.ec.onlinestore.entity.Product;
import com.ec.onlinestore.mapper.OrderMapper;
import com.ec.onlinestore.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    // 销售额排行（商家的商品）
    @GetMapping("/sales-rank/{userId}")
    public List<Map<String, Object>> getSalesRank(@PathVariable Integer userId) {
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("user_id", userId);
        List<Product> products = productMapper.selectList(productWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Product product : products) {
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("product_id", product.getId())
                    .eq("order_status", "delivered");
            List<Order> orders = orderMapper.selectList(orderWrapper);

            BigDecimal totalSales = orders.stream()
                    .map(Order::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", product.getId());
                item.put("productName", product.getName());
                item.put("totalSales", totalSales);
                result.add(item);
            }
        }
        result.sort((a, b) -> ((BigDecimal)b.get("totalSales")).compareTo((BigDecimal)a.get("totalSales")));
        return result.stream().limit(10).collect(Collectors.toList());
    }

    // 统计汇总
    @GetMapping("/summary/{userId}")
    public Map<String, Object> getSummary(@PathVariable Integer userId) {
        // 获取商家的所有商品ID
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("user_id", userId);
        List<Product> products = productMapper.selectList(productWrapper);
        List<Integer> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

        BigDecimal totalSales = BigDecimal.ZERO;
        int totalQuantity = 0;
        int totalOrders = 0;

        if (!productIds.isEmpty()) {
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.in("product_id", productIds);
            List<Order> orders = orderMapper.selectList(orderWrapper);

            totalOrders = orders.size();
            for (Order order : orders) {
                if (order.getTotalPrice() != null) {
                    totalSales = totalSales.add(order.getTotalPrice());
                }
                totalQuantity += order.getQuantity();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalSales", totalSales);
        result.put("totalQuantity", totalQuantity);
        result.put("totalOrders", totalOrders);
        return result;
    }

    // 销售量排行
    @GetMapping("/quantity-rank/{userId}")
    public List<Map<String, Object>> getQuantityRank(@PathVariable Integer userId) {
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("user_id", userId);
        List<Product> products = productMapper.selectList(productWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Product product : products) {
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("product_id", product.getId())
                    .eq("order_status", "delivered");
            List<Order> orders = orderMapper.selectList(orderWrapper);

            int totalQuantity = orders.stream()
                    .mapToInt(Order::getQuantity)
                    .sum();

            if (totalQuantity > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", product.getId());
                item.put("productName", product.getName());
                item.put("totalQuantity", totalQuantity);
                result.add(item);
            }
        }
        result.sort((a, b) -> ((Integer)b.get("totalQuantity")).compareTo((Integer)a.get("totalQuantity")));
        return result.stream().limit(10).collect(Collectors.toList());
    }

    // 近期新品（按ID倒序，最新的前5个）
    @GetMapping("/new-products/{userId}")
    public List<Product> getNewProducts(@PathVariable Integer userId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("id")
                .last("LIMIT 5");
        return productMapper.selectList(wrapper);
    }
}