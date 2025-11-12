// src/main/java/com/barlog/loyaltyapi/service/MenuItemService.java
package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.MenuItemRequestDto;
import com.barlog.loyaltyapi.dto.MenuItemResponseDto;
import com.barlog.loyaltyapi.model.MenuItem;
import com.barlog.loyaltyapi.repository.MenuItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    @Transactional
    public MenuItemResponseDto createMenuItem(MenuItemRequestDto menuItemDto) {
        MenuItem menuItem = MenuItem.builder()
                .name(menuItemDto.name())
                .description(menuItemDto.description())
                .price(menuItemDto.price())
                .category(menuItemDto.category())
                .volume(menuItemDto.volume())
                .icon(menuItemDto.icon())
                .orderInMenu(menuItemDto.orderInMenu())
                .isActive(menuItemDto.isActive())
                .build();
        MenuItem savedItem = menuItemRepository.save(menuItem);
        return mapToDto(savedItem);
    }

    public List<MenuItemResponseDto> getAllActiveMenuItems() {
        return menuItemRepository.findByIsActiveTrueOrderByCategoryAscOrderInMenuAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponseDto> getAllMenuItemsForAdmin() {
        return menuItemRepository.findAllByOrderByCategoryAscOrderInMenuAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public MenuItemResponseDto getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Item-ul de meniu cu ID-ul " + id + " nu a fost găsit."));
    }

    @Transactional
    public MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto menuItemDto) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item-ul de meniu cu ID-ul " + id + " nu a fost găsit."));

        menuItem.setName(menuItemDto.name());
        menuItem.setDescription(menuItemDto.description());
        menuItem.setPrice(menuItemDto.price());
        menuItem.setCategory(menuItemDto.category());
        menuItem.setVolume(menuItemDto.volume());
        menuItem.setIcon(menuItemDto.icon());
        menuItem.setOrderInMenu(menuItemDto.orderInMenu());
        menuItem.setActive(menuItemDto.isActive()); // Adminul poate activa/dezactiva

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return mapToDto(updatedItem);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        // Implementăm ștergerea logică (setăm isActive pe false) pentru a păstra istoricul
        // Sau o ștergere fizică dacă nu e nevoie de istoric
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item-ul de meniu cu ID-ul " + id + " nu a fost găsit."));
        menuItem.setActive(false); // Ștergere logică
        menuItemRepository.save(menuItem);
        // Dacă vrei ștergere fizică: menuItemRepository.delete(menuItem);
    }
    
    // Metoda de mapare
    protected MenuItemResponseDto mapToDto(MenuItem menuItem) {
        MenuItemResponseDto dto = new MenuItemResponseDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setCategory(menuItem.getCategory());
        dto.setVolume(menuItem.getVolume());
        dto.setIcon(menuItem.getIcon());
        dto.setOrderInMenu(menuItem.getOrderInMenu());
        dto.setActive(menuItem.isActive());
        return dto;
    }
}