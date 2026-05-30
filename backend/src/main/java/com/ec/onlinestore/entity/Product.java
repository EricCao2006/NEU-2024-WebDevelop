package com.ec.onlinestore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String shortDescription;
    private String longDescription;
    private String category;
    private String imageUrl;
    private Integer userId;
}