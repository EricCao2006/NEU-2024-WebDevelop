package com.ec.onlinestore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ec.onlinestore.entity.Order;
import com.ec.onlinestore.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public boolean createOrder(Order order) {
        order.setOrderTime(LocalDateTime.now());
        order.setOrderStatus("pending");
        return orderMapper.insert(order) > 0;
    }

    public List<Order> getOrdersByUser(Integer userId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return orderMapper.selectList(wrapper);
    }

    public List<Order> getAllOrders() {
        return orderMapper.selectList(null);
    }

    public boolean updateOrderStatus(Integer id, String status) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            order.setOrderStatus(status);
            return orderMapper.updateById(order) > 0;
        }
        return false;
    }
}