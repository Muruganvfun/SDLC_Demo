package com.oms.catalog.service;

import com.oms.catalog.dto.ProductRequest;
import com.oms.catalog.dto.ProductResponse;
import com.oms.catalog.entity.Product;
import com.oms.catalog.repository.ProductRepository;
import com.oms.common.dto.PageResponse;
import com.oms.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .imageUrl("/images/test.jpg")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Get Products Tests")
    class GetProductsTests {

        @Test
        @DisplayName("Should return paginated products")
        void getProducts_ShouldReturnPaginatedProducts() {
            // Given
            List<Product> products = List.of(testProduct);
            Page<Product> productPage = new PageImpl<>(products);
            when(productRepository.findByActiveTrue(any(PageRequest.class))).thenReturn(productPage);

            // When
            PageResponse<ProductResponse> response = productService.getProducts(0, 20, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getName()).isEqualTo("Test Product");
            assertThat(response.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should filter by active status when specified")
        void getProducts_WithActiveFilter_ShouldFilterProducts() {
            // Given
            Page<Product> productPage = new PageImpl<>(List.of(testProduct));
            when(productRepository.findByActive(eq(true), any(PageRequest.class))).thenReturn(productPage);

            // When
            PageResponse<ProductResponse> response = productService.getProducts(0, 20, true);

            // Then
            assertThat(response.getContent()).hasSize(1);
            verify(productRepository).findByActive(eq(true), any(PageRequest.class));
        }

        @Test
        @DisplayName("Should limit page size to 100")
        void getProducts_WithLargePageSize_ShouldLimitTo100() {
            // Given
            Page<Product> productPage = new PageImpl<>(List.of());
            when(productRepository.findByActiveTrue(any(PageRequest.class))).thenReturn(productPage);

            // When
            productService.getProducts(0, 500, null);

            // Then
            verify(productRepository).findByActiveTrue(argThat(pageRequest -> 
                pageRequest.getPageSize() == 100
            ));
        }
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should return product when found")
        void getProduct_WhenFound_ShouldReturnProduct() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            // When
            ProductResponse response = productService.getProduct(productId.toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(productId.toString());
            assertThat(response.getName()).isEqualTo("Test Product");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void getProduct_WhenNotFound_ShouldThrowException() {
            // Given
            UUID invalidId = UUID.randomUUID();
            when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.getProduct(invalidId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product when user is admin")
        void createProduct_AsAdmin_ShouldCreateProduct() {
            // Given
            ProductRequest request = new ProductRequest();
            request.setName("New Product");
            request.setDescription("New Description");
            request.setPrice(new BigDecimal("49.99"));
            request.setImageUrl("/images/new.jpg");

            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                product.setId(UUID.randomUUID());
                product.setCreatedAt(Instant.now());
                product.setUpdatedAt(Instant.now());
                return product;
            });

            // When
            ProductResponse response = productService.createProduct(request, true);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("New Product");
            assertThat(response.isActive()).isTrue();
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to create")
        void createProduct_AsNonAdmin_ShouldThrowException() {
            // Given
            ProductRequest request = new ProductRequest();
            request.setName("New Product");

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(request, false))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Only admins can create products");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should initialize inventory when initial stock provided")
        void createProduct_WithInitialStock_ShouldInitializeInventory() {
            // Given
            ProductRequest request = new ProductRequest();
            request.setName("New Product");
            request.setDescription("Description");
            request.setPrice(new BigDecimal("29.99"));
            request.setInitialStock(100);

            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                product.setId(UUID.randomUUID());
                product.setCreatedAt(Instant.now());
                return product;
            });

            // When
            productService.createProduct(request, true);

            // Then
            verify(restTemplate).postForObject(
                    contains("/inventory/initialize"),
                    any(),
                    eq(Void.class)
            );
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product when user is admin")
        void updateProduct_AsAdmin_ShouldUpdateProduct() {
            // Given
            ProductRequest request = new ProductRequest();
            request.setName("Updated Name");
            request.setPrice(new BigDecimal("79.99"));

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductResponse response = productService.updateProduct(productId.toString(), request, true);

            // Then
            assertThat(response).isNotNull();
            verify(productRepository).save(argThat(product ->
                    product.getName().equals("Updated Name") &&
                    product.getPrice().compareTo(new BigDecimal("79.99")) == 0
            ));
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to update")
        void updateProduct_AsNonAdmin_ShouldThrowException() {
            // Given
            ProductRequest request = new ProductRequest();
            request.setName("Updated Name");

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(productId.toString(), request, false))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Only admins can update products");
        }

        @Test
        @DisplayName("Should only update provided fields")
        void updateProduct_WithPartialData_ShouldOnlyUpdateProvidedFields() {
            // Given
            ProductRequest request = new ProductRequest();
            request.setName("Updated Name");
            // description and price are null

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(productId.toString(), request, true);

            // Then
            verify(productRepository).save(argThat(product ->
                    product.getName().equals("Updated Name") &&
                    product.getDescription().equals("Test Description") // unchanged
            ));
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should soft delete product when user is admin")
        void deleteProduct_AsAdmin_ShouldSoftDelete() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            // When
            productService.deleteProduct(productId.toString(), true);

            // Then
            verify(productRepository).save(argThat(product -> !product.isActive()));
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to delete")
        void deleteProduct_AsNonAdmin_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(productId.toString(), false))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Only admins can delete products");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void deleteProduct_WhenNotFound_ShouldThrowException() {
            // Given
            UUID invalidId = UUID.randomUUID();
            when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(invalidId.toString(), true))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Product not found");
        }
    }
}
