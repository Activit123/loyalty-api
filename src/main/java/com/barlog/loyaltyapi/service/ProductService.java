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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    public ProductResponseDto createProduct(ProductRequestDto productDto, MultipartFile imageFile) {
        // Salvează imaginea pe Cloudinary și obține Public ID-ul
        String publicId = fileStorageService.storeFile(imageFile);

        Product product = Product.builder()
                .name(productDto.name())
                .description(productDto.description())
                .buyPrice(productDto.buyPrice())
                .claimValue(productDto.claimValue())
                .stock(productDto.stock())
                .category(productDto.category())
                .imageUrl(publicId) // *** STOCARE PUBLIC ID ***
                .isActive(true)
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }


    // Metodă nouă pentru a găsi un produs pe baza numelui exact
    public Optional<Product> findProductByName(String name) {
        return productRepository.findByName(name); // Presupune că ProductRepository are findByName
    }

    public Product matchProductByFormattedDescription(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }

        String productName = description.trim();
        String lowerDesc = productName.toLowerCase();

        // 1. Verifică formatul de Revendicare (Admin Scanner / Bar)
        if (lowerDesc.startsWith("revendicare:")) {
            productName = productName.substring("revendicare:".length()).trim();
        }
        // 2. NOU: Verifică formatul de Cumpărare (Shop App)
        // Verificăm și varianta cu diacritice și fără, pentru siguranță
        else if (lowerDesc.startsWith("cumpărat produs:")) {
            productName = productName.substring("Cumpărat produs:".length()).trim();
        }
        else if (lowerDesc.startsWith("cumparat produs:")) {
            productName = productName.substring("Cumparat produs:".length()).trim();
        }
        else {
            // Nu este o tranzacție legată de un produs
            return null;
        }

        // 3. Caută produsul după numele exact extras
        return productRepository.findByName(productName)
                .orElse(null);
    }
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findByIsActiveTrueOrderByIdDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    protected ProductResponseDto mapToDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBuyPrice(product.getBuyPrice());
        dto.setClaimValue(product.getClaimValue());
        dto.setStock(product.getStock());
        dto.setCategory(product.getCategory());

        // *** GENERARE DINAMICĂ A URL-ULUI ***
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            dto.setImageUrl(fileStorageService.getImageUrlFromPublicId(product.getImageUrl()));
        } else {
            dto.setImageUrl(null);
        }

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
            String newPublicId = fileStorageService.storeFile(imageFile);

            // --- LOGICĂ OPȚIONALĂ PENTRU ȘTERGEREA IMAGINII VECHI ---
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                fileStorageService.deleteFile(product.getImageUrl());
            }
            // --------------------------------------------------------

            product.setImageUrl(newPublicId); // Update DB cu noul Public ID
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    // --- METODĂ NOUĂ PENTRU DELETE ---
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produsul cu ID-ul " + productId + " nu a fost găsit."));

        // --- LOGICĂ OPȚIONALĂ PENTRU ȘTERGEREA IMAGINII VECHI ---
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(product.getImageUrl());
        }
        // --------------------------------------------------------

        // În loc să ștergem, setăm produsul ca fiind inactiv
        product.setActive(false);
        productRepository.save(product);
    }
}