package com.ec.onlinestore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String category;
    private String imageUrl;
}