package com.ec.onlinestore.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ec.onlinestore.entity.Product;
import com.ec.onlinestore.entity.ProductSku;
import com.ec.onlinestore.mapper.ProductSkuMapper;
import com.ec.onlinestore.service.ProductService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    private final ProductSkuMapper productSkuMapper;

    public ProductController(ProductService productService, ProductSkuMapper productSkuMapper) {
        this.productService = productService;
        this.productSkuMapper = productSkuMapper;
    }

    // 获取所有商品（含最低价）
    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Product> products = productService.getAllProducts();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (Product product : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("shortDescription", product.getShortDescription());
            item.put("category", product.getCategory());
            item.put("imageUrl", product.getImageUrl());
            item.put("userId", product.getUserId());

            QueryWrapper<ProductSku> wrapper = new QueryWrapper<>();
            wrapper.eq("product_id", product.getId());
            wrapper.orderByAsc("price");
            List<ProductSku> skus = productSkuMapper.selectList(wrapper);
            if (!skus.isEmpty()) {
                item.put("minPrice", skus.get(0).getPrice());
                item.put("totalStock", skus.stream().mapToInt(ProductSku::getStock).sum());
            } else {
                item.put("minPrice", null);
                item.put("totalStock", 0);
            }
            result.add(item);
        }
        return result;
    }

    // 搜索商品
    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam(required = false) String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (Product product : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("shortDescription", product.getShortDescription());
            item.put("category", product.getCategory());
            item.put("imageUrl", product.getImageUrl());
            item.put("userId", product.getUserId());

            QueryWrapper<ProductSku> wrapper = new QueryWrapper<>();
            wrapper.eq("product_id", product.getId());
            wrapper.orderByAsc("price");
            List<ProductSku> skus = productSkuMapper.selectList(wrapper);
            if (!skus.isEmpty()) {
                item.put("minPrice", skus.get(0).getPrice());
                item.put("totalStock", skus.stream().mapToInt(ProductSku::getStock).sum());
            } else {
                item.put("minPrice", null);
                item.put("totalStock", 0);
            }
            result.add(item);
        }
        return result;
    }

    // 获取商品详情（含所有款式）
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Product product = productService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getName());
        result.put("shortDescription", product.getShortDescription());
        result.put("longDescription", product.getLongDescription());
        result.put("category", product.getCategory());
        result.put("imageUrl", product.getImageUrl());
        result.put("userId", product.getUserId());

        QueryWrapper<ProductSku> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", id);
        List<ProductSku> skus = productSkuMapper.selectList(wrapper);
        result.put("skus", skus);

        if (!skus.isEmpty()) {
            BigDecimal minPrice = skus.stream().map(ProductSku::getPrice).min(BigDecimal::compareTo).orElse(null);
            result.put("minPrice", minPrice);
        }

        return result;
    }

    // 获取商品的所有款式
    @GetMapping("/{productId}/skus")
    public List<ProductSku> getSkusByProductId(@PathVariable Integer productId) {
        QueryWrapper<ProductSku> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        return productSkuMapper.selectList(wrapper);
    }

    // 补货接口
    @PostMapping("/refill")
    public Map<String, Object> refillStock(@RequestBody Map<String, Integer> body) {
        Integer productId = body.get("productId");
        Integer skuId = body.get("skuId");
        Integer quantity = body.get("quantity");

        if (productId == null || quantity == null || quantity <= 0) {
            return Map.of("success", false, "message", "参数错误");
        }

        // 如果有指定款式ID
        if (skuId != null && skuId > 0) {
            ProductSku sku = productSkuMapper.selectById(skuId);
            if (sku != null) {
                sku.setStock(sku.getStock() + quantity);
                productSkuMapper.updateById(sku);
                return Map.of("success", true);
            }
        } else {
            // 没有指定款式，补货到第一个款式
            QueryWrapper<ProductSku> wrapper = new QueryWrapper<>();
            wrapper.eq("product_id", productId);
            wrapper.last("LIMIT 1");
            List<ProductSku> skus = productSkuMapper.selectList(wrapper);
            if (skus != null && !skus.isEmpty()) {
                ProductSku sku = skus.get(0);
                sku.setStock(sku.getStock() + quantity);
                productSkuMapper.updateById(sku);
                return Map.of("success", true);
            }
        }

        return Map.of("success", false, "message", "补货失败");
    }

    // 商家接口：新增商品
    @PostMapping("/merchant")
    public Map<String, Object> add(@RequestBody Product product) {
        boolean success = productService.addProduct(product);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "添加成功" : "添加失败");
        if (success) {
            result.put("id", product.getId());  // MyBatis-Plus 会自动回填 ID
        }
        return result;
    }

    // 添加商品款式
    @PostMapping("/product-sku")
    public Map<String, Object> addProductSku(@RequestBody ProductSku sku) {
        int rows = productSkuMapper.insert(sku);
        return Map.of("success", rows > 0, "message", rows > 0 ? "款式添加成功" : "款式添加失败");
    }

    // 商家接口：删除商品
    @DeleteMapping("/merchant/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        // 先删除该商品的所有款式
        QueryWrapper<ProductSku> skuWrapper = new QueryWrapper<>();
        skuWrapper.eq("product_id", id);
        productSkuMapper.delete(skuWrapper);

        // 再删除商品
        boolean success = productService.deleteProduct(id);
        return Map.of("success", success, "message", success ? "删除成功" : "删除失败");
    }

    @GetMapping("/merchant/{userId}")
    public List<Map<String, Object>> getByMerchant(@PathVariable Integer userId) {
        List<Product> products = productService.getProductsByMerchant(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Product product : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("shortDescription", product.getShortDescription());
            item.put("longDescription", product.getLongDescription());
            item.put("category", product.getCategory());
            item.put("imageUrl", product.getImageUrl());
            item.put("userId", product.getUserId());

            // 获取款式最低价和总库存
            QueryWrapper<ProductSku> skuWrapper = new QueryWrapper<>();
            skuWrapper.eq("product_id", product.getId());
            List<ProductSku> skus = productSkuMapper.selectList(skuWrapper);

            if (skus != null && !skus.isEmpty()) {
                BigDecimal minPrice = skus.stream()
                        .map(ProductSku::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                Integer totalStock = skus.stream()
                        .mapToInt(ProductSku::getStock)
                        .sum();
                item.put("minPrice", minPrice);
                item.put("totalStock", totalStock);
            } else {
                item.put("minPrice", null);
                item.put("totalStock", 0);
            }
            result.add(item);
        }
        return result;
    }
}