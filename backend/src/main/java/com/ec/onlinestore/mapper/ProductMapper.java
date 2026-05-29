package com.ec.onlinestore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ec.onlinestore.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}