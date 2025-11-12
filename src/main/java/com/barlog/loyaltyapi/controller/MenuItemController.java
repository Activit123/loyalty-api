// src/main/java/com/barlog/loyaltyapi/controller/MenuItemController.java
package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.MenuItemRequestDto;
import com.barlog.loyaltyapi.dto.MenuItemResponseDto;
import com.barlog.loyaltyapi.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // Public: Obține toate elementele active din meniu (pentru vizualizare user)
    @GetMapping
    public ResponseEntity<List<MenuItemResponseDto>> getAllActiveMenuItems() {
        List<MenuItemResponseDto> menuItems = menuItemService.getAllActiveMenuItems();
        return ResponseEntity.ok(menuItems);
    }

    // Admin: Obține toate elementele din meniu (inclusiv inactive)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MenuItemResponseDto>> getAllMenuItemsForAdmin() {
        List<MenuItemResponseDto> menuItems = menuItemService.getAllMenuItemsForAdmin();
        return ResponseEntity.ok(menuItems);
    }

    // Admin: Obține un singur element din meniu după ID
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> getMenuItemById(@PathVariable Long id) {
        MenuItemResponseDto menuItem = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(menuItem);
    }

    // Admin: Creează un nou element de meniu
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> createMenuItem(@Valid @RequestBody MenuItemRequestDto menuItemDto) {
        MenuItemResponseDto newItem = menuItemService.createMenuItem(menuItemDto);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    // Admin: Actualizează un element de meniu existent
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponseDto> updateMenuItem(@PathVariable Long id, @Valid @RequestBody MenuItemRequestDto menuItemDto) {
        MenuItemResponseDto updatedItem = menuItemService.updateMenuItem(id, menuItemDto);
        return ResponseEntity.ok(updatedItem);
    }

    // Admin: Șterge logic un element de meniu (îl setează inactiv)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}