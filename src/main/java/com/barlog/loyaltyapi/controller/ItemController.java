package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.ItemTemplateRequestDto;
import com.barlog.loyaltyapi.dto.QrCodeListDto;
import com.barlog.loyaltyapi.dto.QrCodeResponse;
import com.barlog.loyaltyapi.dto.UserItemDto;
import com.barlog.loyaltyapi.model.ItemTemplate;
import com.barlog.loyaltyapi.model.QrCode;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.service.ItemService;
import com.barlog.loyaltyapi.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final QrCodeService qrCodeService;

    @GetMapping("/template/{id}")
    @PreAuthorize("permitAll()") // Sau "isAuthenticated()"
    public ResponseEntity<com.barlog.loyaltyapi.dto.UserItemDto> getItemTemplateDetails(@PathVariable Long id) {
        // Folosim metoda din service pentru a găsi și mapa template-ul
        ItemTemplate template = itemService.getItemTemplateById(id);
        return ResponseEntity.ok(itemService.mapTemplateToDto(template));
    }
    // --- ADMIN: Creare Item ---
    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemTemplate> createItem(
            @RequestPart("item") ItemTemplateRequestDto itemDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
        ItemTemplate createdItem = itemService.createItemTemplate(itemDto, imageFile);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }
    // --- ADMIN: Activează/Dezactivează Item ---
    @PatchMapping("/admin/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemTemplate> toggleItemStatus(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.toggleItemStatus(id));
    }
    // Endpoint pentru SHOP (Useri) - Doar Active
    @GetMapping("/shop")
    public ResponseEntity<List<ItemTemplate>> getShopItems() {
        return ResponseEntity.ok(itemService.getShopItems());
    }

    // Endpoint pentru ADMIN - TOATE (Active + Inactive)
    // Asta te asigură că vezi tot ce ai creat, chiar dacă isActive = false
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ItemTemplate>> getAllItemsForAdmin() {
        return ResponseEntity.ok(itemService.getAllItemsForAdmin());
    }

    // --- SHOP: Cumpărare Item ---
    @PostMapping("/buy/{templateId}")
    public ResponseEntity<String> buyItem(Authentication authentication, @PathVariable Long templateId) {
        User user = (User) authentication.getPrincipal();
        itemService.purchaseItem(user, templateId);
        return ResponseEntity.ok("Item cumpărat cu succes!");
    }

    // Admin: Generează cod QR pentru Loot
    @PostMapping("/admin/generate-loot/{itemTemplateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QrCodeResponse> generateLootQr(@PathVariable Long itemTemplateId) {
        return ResponseEntity.ok(qrCodeService.generateQrForLoot(itemTemplateId));
    }

    // User: Revendică Loot prin scanare
    // Acesta este endpoint-ul pe care îl apelează Userul când scanează codul
    @PostMapping("/claim-loot/{code}")
    public ResponseEntity<String> claimLoot(Authentication authentication, @PathVariable String code) {
        User user = (User) authentication.getPrincipal();
        String result = qrCodeService.claimLoot(user, code);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/admin/loot-codes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QrCodeListDto>> getAllLootCodes() {
        return ResponseEntity.ok(qrCodeService.getAllLootCodes());
    }
    // --- INVENTORY: Vezi Inventarul ---
    @GetMapping("/inventory")
    public ResponseEntity<List<UserItemDto>> getInventory(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(itemService.getUserInventory(user));
    }
    @PutMapping(value = "/admin/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemTemplate> updateItem(
            @PathVariable Long id,
            @RequestPart("item") ItemTemplateRequestDto itemDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        ItemTemplate updatedItem = itemService.updateItem(id, itemDto, imageFile);
        return ResponseEntity.ok(updatedItem);
    }
    // --- INVENTORY: Echipează ---
    @PostMapping("/equip/{userItemId}")
    public ResponseEntity<String> equipItem(Authentication authentication, @PathVariable Long userItemId) {
        User user = (User) authentication.getPrincipal();
        itemService.equipItem(user, userItemId);
        return ResponseEntity.ok("Item echipat!");
    }

    // --- INVENTORY: Dezechipează ---
    @PostMapping("/unequip/{userItemId}")
    public ResponseEntity<String> unequipItem(Authentication authentication, @PathVariable Long userItemId) {
        User user = (User) authentication.getPrincipal();
        itemService.unequipItem(user, userItemId);
        return ResponseEntity.ok("Item dezechipat!");
    }
}