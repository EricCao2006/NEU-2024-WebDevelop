package com.ec.onlinestore.service;

import com.ec.onlinestore.entity.Order;
import com.ec.onlinestore.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public boolean createOrder(Order order) {
        order.setOrderTime(LocalDateTime.now());
        order.setOrderStatus("pending");
        return orderMapper.insert(order) > 0;
    }

    public List<Order> getAllOrders() {
        return orderMapper.selectList(null);
    }

}