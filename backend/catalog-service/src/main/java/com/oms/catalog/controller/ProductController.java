package com.oms.catalog.controller;

import com.oms.catalog.dto.ProductRequest;
import com.oms.catalog.dto.ProductResponse;
import com.oms.catalog.service.ProductService;
import com.oms.common.dto.ApiResponse;
import com.oms.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean active) {
        log.info("Getting products - page: {}, size: {}, active: {}", page, size, active);
        PageResponse<ProductResponse> products = productService.getProducts(page, size, active);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable String id) {
        log.info("Getting product: {}", id);
        ProductResponse product = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) {
        log.info("Creating product: {}", request.getName());
        boolean isAdmin = roles.contains("ADMIN");
        ProductResponse product = productService.createProduct(request, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) {
        log.info("Updating product: {}", id);
        boolean isAdmin = roles.contains("ADMIN");
        ProductResponse product = productService.updateProduct(id, request, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) {
        log.info("Deleting product: {}", id);
        boolean isAdmin = roles.contains("ADMIN");
        productService.deleteProduct(id, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
