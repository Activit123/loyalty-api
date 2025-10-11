package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ProductRequestDto;
import com.barlog.loyaltyapi.dto.ProductResponseDto;
import com.barlog.loyaltyapi.model.Product;
import com.barlog.loyaltyapi.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    public ProductResponseDto createProduct(ProductRequestDto productDto, MultipartFile imageFile) {
        // Salvează imaginea și obține numele unic
        String fileName = fileStorageService.storeFile(imageFile);
        
        // Construiește URL-ul complet la care imaginea va fi accesibilă
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/images/")
                .path(fileName)
                .toUriString();

        Product product = Product.builder()
                .name(productDto.name())
                .description(productDto.description())
                .buyPrice(productDto.buyPrice())
                .claimValue(productDto.claimValue())
                .stock(productDto.stock())
                .imageUrl(imageUrl) // Salvăm URL-ul complet
                .isActive(true)
                .build();
        
        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Aici pot fi adăugate metode pentru update și delete

    private ProductResponseDto mapToDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBuyPrice(product.getBuyPrice());
        dto.setClaimValue(product.getClaimValue());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.isActive());
        return dto;
    }
}