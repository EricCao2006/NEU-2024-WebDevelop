package com.ec.onlinestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ec.onlinestore.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}