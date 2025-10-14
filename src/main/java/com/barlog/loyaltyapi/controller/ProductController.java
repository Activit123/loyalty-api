package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.ProductRequestDto;
import com.barlog.loyaltyapi.dto.ProductResponseDto;
import com.barlog.loyaltyapi.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Endpoint pentru crearea unui produs nou (doar pentru admin)
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestPart("product") ProductRequestDto productDto,
            @RequestPart("image") MultipartFile imageFile) {
        
        ProductResponseDto newProduct = productService.createProduct(productDto, imageFile);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }
    
    // Endpoint pentru a vedea toate produsele (accesibil public)
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    // --- ENDPOINT NOU PENTRU UPDATE ---
    @PutMapping(value = "/{productId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestPart("product") ProductRequestDto productDto,
            // Imaginea este opțională la update
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        ProductResponseDto updatedProduct = productService.updateProduct(productId, productDto, imageFile);
        return ResponseEntity.ok(updatedProduct);
    }

    // --- ENDPOINT NOU PENTRU DELETE ---
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build(); // Răspuns 204 No Content
    }

}