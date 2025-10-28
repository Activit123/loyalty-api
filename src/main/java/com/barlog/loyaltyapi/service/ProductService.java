package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ProductRequestDto;
import com.barlog.loyaltyapi.dto.ProductResponseDto;
import com.barlog.loyaltyapi.model.Product;
import com.barlog.loyaltyapi.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                .category(productDto.category()) // Adaugă această linie
                .imageUrl(imageUrl) // Salvăm URL-ul complet
                .isActive(true)
                .build();
        
        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    public List<ProductResponseDto> getAllProducts() {
        // Folosim noua metodă pentru a prelua doar produsele active
        return productRepository.findByIsActiveTrueOrderByIdDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Aici pot fi adăugate metode pentru update și delete

    protected ProductResponseDto mapToDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBuyPrice(product.getBuyPrice());
        dto.setClaimValue(product.getClaimValue());
        dto.setStock(product.getStock());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.isActive());
        return dto;
    }
    // --- METODĂ NOUĂ PENTRU UPDATE ---
    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto productDto, MultipartFile imageFile) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produsul cu ID-ul " + productId + " nu a fost găsit."));

        // Actualizăm câmpurile de text
        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setBuyPrice(productDto.buyPrice());
        product.setClaimValue(productDto.claimValue());
        product.setCategory(productDto.category());
        product.setStock(productDto.stock());

        // Dacă a fost încărcată o imagine nouă, o actualizăm
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/images/")
                    .path(fileName)
                    .toUriString();
            product.setImageUrl(imageUrl);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    // --- METODĂ NOUĂ PENTRU DELETE ---
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produsul cu ID-ul " + productId + " nu a fost găsit."));

        // În loc să ștergem, setăm produsul ca fiind inactiv
        product.setActive(false);

        productRepository.save(product);
    }
}