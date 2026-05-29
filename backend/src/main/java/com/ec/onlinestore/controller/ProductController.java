package com.ec.onlinestore.controller;

import com.ec.onlinestore.entity.Product;
import com.ec.onlinestore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam(required = false) String keyword) {
        return productService.searchProducts(keyword);
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Product product) {
        boolean success = productService.addProduct(product);
        return Map.of("success", success, "message", success ? "添加成功" : "添加失败");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody Product product) {
        boolean success = productService.updateProduct(id, product);
        return Map.of("success", success, "message", success ? "修改成功" : "修改失败");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        boolean success = productService.deleteProduct(id);
        return Map.of("success", success, "message", success ? "删除成功" : "删除失败");
    }
}