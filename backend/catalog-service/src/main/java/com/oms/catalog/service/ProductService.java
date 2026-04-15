package com.oms.catalog.service;

import com.oms.catalog.dto.ProductRequest;
import com.oms.catalog.dto.ProductResponse;
import com.oms.catalog.entity.Product;
import com.oms.catalog.repository.ProductRepository;
import com.oms.common.client.dto.InitializeInventoryRequest;
import com.oms.common.client.dto.InventoryInfo;
import com.oms.common.dto.PageResponse;
import com.oms.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    @Value("${services.inventory-url:http://localhost:8084}")
    private String inventoryServiceUrl;

    public PageResponse<ProductResponse> getProducts(int page, int size, Boolean active) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        
        Page<Product> productPage;
        if (active != null) {
            productPage = productRepository.findByActive(active, pageRequest);
        } else {
            productPage = productRepository.findByActiveTrue(pageRequest);
        }

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichWithStock)
                .collect(Collectors.toList());

        return PageResponse.of(products, page, size, productPage.getTotalElements());
    }

    public ProductResponse getProduct(String id) {
        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        return enrichWithStock(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, boolean isAdmin) {
        if (!isAdmin) {
            throw ApiException.forbidden("Only admins can create products");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        product = productRepository.save(product);
        log.info("Product created: {}", product.getId());

        if (request.getInitialStock() != null && request.getInitialStock() > 0) {
            initializeInventory(product.getId().toString(), request.getInitialStock());
        }

        return ProductResponse.from(product, request.getInitialStock());
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request, boolean isAdmin) {
        if (!isAdmin) {
            throw ApiException.forbidden("Only admins can update products");
        }

        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> ApiException.notFound("Product not found"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        product = productRepository.save(product);
        log.info("Product updated: {}", product.getId());

        return enrichWithStock(product);
    }

    @Transactional
    public void deleteProduct(String id, boolean isAdmin) {
        if (!isAdmin) {
            throw ApiException.forbidden("Only admins can delete products");
        }

        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> ApiException.notFound("Product not found"));

        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
    }

    private ProductResponse enrichWithStock(Product product) {
        try {
            String url = inventoryServiceUrl + "/inventory/" + product.getId();
            InventoryInfo inventory = restTemplate.getForObject(url, InventoryInfo.class);
            if (inventory != null) {
                return ProductResponse.from(product, inventory.getAvailableQuantity());
            }
        } catch (Exception e) {
            log.debug("Could not fetch inventory for product {}: {}", product.getId(), e.getMessage());
        }
        return ProductResponse.from(product);
    }

    private void initializeInventory(String productId, int quantity) {
        try {
            String url = inventoryServiceUrl + "/inventory/initialize";
            InitializeInventoryRequest request = InitializeInventoryRequest.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            restTemplate.postForObject(url, request, Void.class);
        } catch (Exception e) {
            log.warn("Could not initialize inventory for product {}: {}", productId, e.getMessage());
        }
    }
}
