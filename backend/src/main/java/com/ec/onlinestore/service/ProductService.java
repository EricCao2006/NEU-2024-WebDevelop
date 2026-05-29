package com.ec.onlinestore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ec.onlinestore.entity.Product;
import com.ec.onlinestore.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> getAllProducts() {
        return productMapper.selectList(null);
    }

    public List<Product> searchProducts(String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like("name", keyword).or().like("category", keyword);
        }
        return productMapper.selectList(wrapper);
    }

    public boolean addProduct(Product product) {
        return productMapper.insert(product) > 0;
    }

    public boolean updateProduct(Integer id, Product product) {
        product.setId(id);
        return productMapper.updateById(product) > 0;
    }

    public boolean deleteProduct(Integer id) {
        return productMapper.deleteById(id) > 0;
    }
}