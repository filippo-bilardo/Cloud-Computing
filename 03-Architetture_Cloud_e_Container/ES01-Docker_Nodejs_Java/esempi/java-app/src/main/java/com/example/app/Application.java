package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.management.ManagementFactory;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/api")
class ProductController {
    
    private final Map<Integer, Product> products = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    
    public ProductController() {
        // Initialize with sample data
        addProduct(new Product(0, "Laptop", 999.99, "Electronics"));
        addProduct(new Product(0, "Mouse", 29.99, "Electronics"));
        addProduct(new Product(0, "Keyboard", 79.99, "Electronics"));
    }
    
    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Java API");
        info.put("message", "Running in Docker container");
        info.put("version", "1.0.0");
        info.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s");
        return info;
    }
    
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }
    
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable int id) {
        Product product = products.get(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
    
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        product.setId(idCounter.getAndIncrement());
        products.put(product.getId(), product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product updatedProduct) {
        Product product = products.get(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        updatedProduct.setId(id);
        products.put(id, updatedProduct);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        if (products.remove(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
    
    private void addProduct(Product product) {
        product.setId(idCounter.getAndIncrement());
        products.put(product.getId(), product);
    }
}

class Product {
    private int id;
    private String name;
    private double price;
    private String category;
    
    public Product() {}
    
    public Product(int id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
