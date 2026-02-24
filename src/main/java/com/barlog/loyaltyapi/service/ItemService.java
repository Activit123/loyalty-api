package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ItemEffectDto;
import com.barlog.loyaltyapi.dto.ItemTemplateRequestDto;
import com.barlog.loyaltyapi.dto.UserItemDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemTemplateRepository itemTemplateRepository;
    private final UserItemRepository userItemRepository;
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final FileStorageService fileStorageService;
    private final BonusService bonusService;

    // --- ADMIN: Creare Item ---

    public List<ItemTemplate> getAllItemsForAdmin() {
        List<ItemTemplate> items = itemTemplateRepository.findAll();

        // Procesăm fiecare item pentru a genera URL-ul complet al imaginii
        items.forEach(item -> {
            if (item.getImageUrl() != null && !item.getImageUrl().startsWith("http")) {
                item.setImageUrl(fileStorageService.getImageUrlFromPublicId(item.getImageUrl()));
            }
        });

        return items;
    }

    // --- SHOP: Returnează DOAR itemele ACTIVE (cu URL procesat) ---
    public List<ItemTemplate> getShopItems() {
        List<ItemTemplate> items = itemTemplateRepository.findByIsActiveTrue();

        items.forEach(item -> {
            if (item.getImageUrl() != null && !item.getImageUrl().startsWith("http")) {
                item.setImageUrl(fileStorageService.getImageUrlFromPublicId(item.getImageUrl()));
            }
        });

        return items;
    }

    @Transactional
    public ItemTemplate toggleItemStatus(Long id) {
        ItemTemplate item = itemTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item-ul nu a fost găsit."));

        // Inversăm statusul curent
        item.setActive(!item.isActive());

        return itemTemplateRepository.save(item);
    }



    public ItemTemplate getItemTemplateById(Long id) {
        return itemTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item-ul nu a fost găsit."));
    }

    // Helper pentru mapare (dacă nu există deja)
    public UserItemDto mapTemplateToDto(ItemTemplate template) {
        UserItemDto dto = new UserItemDto();
        dto.setTemplateId(template.getId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());

        if (template.getImageUrl() != null && !template.getImageUrl().startsWith("http")) {
            dto.setImageUrl(fileStorageService.getImageUrlFromPublicId(template.getImageUrl()));
        } else {
            dto.setImageUrl(template.getImageUrl());
        }

        dto.setSlot(template.getSlot());
        dto.setRarity(template.getRarity());
        dto.setMinLevel(template.getMinLevel());
        dto.setReqStr(template.getReqStr());
        dto.setReqDex(template.getReqDex());
        dto.setReqInt(template.getReqInt());
        dto.setReqCha(template.getReqCha());

        if (template.getEffects() != null) {
            List<ItemEffectDto> effectDtos = template.getEffects().stream()
                    .map(eff -> {
                        ItemEffectDto eDto = new ItemEffectDto();
                        eDto.setEffectType(eff.getEffectType());
                        eDto.setValue(eff.getValue());
                        eDto.setTargetCategory(eff.getTargetCategory());
                        return eDto;
                    }).collect(Collectors.toList());
            dto.setEffects(effectDtos);
        }

        return dto;
    }

    @Transactional
    public ItemTemplate createItemTemplate(ItemTemplateRequestDto dto, MultipartFile imageFile) {
        String publicId = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            publicId = fileStorageService.storeFile(imageFile);
        }

        ItemTemplate item = ItemTemplate.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(publicId)
                .slot(dto.getSlot())
                .rarity(dto.getRarity())
                .minLevel(dto.getMinLevel() != null ? dto.getMinLevel() : 1)
                .buyPrice(dto.getBuyPrice())
                .sellPrice(dto.getSellPrice())
                .isActive(dto.isActive())
                .reqStr(dto.getReqStr() != null ? dto.getReqStr() : 0)
                .reqDex(dto.getReqDex() != null ? dto.getReqDex() : 0)
                .reqInt(dto.getReqInt() != null ? dto.getReqInt() : 0)
                .reqCha(dto.getReqCha() != null ? dto.getReqCha() : 0)
                .build();

        // Mapare efecte
        if (dto.getEffects() != null) {
            List<ItemEffect> effects = dto.getEffects().stream().map(eDto -> ItemEffect.builder()
                    .itemTemplate(item) // Legătura cu părintele
                    .effectType(eDto.getEffectType())
                    .value(eDto.getValue())
                    .targetCategory(eDto.getTargetCategory())
                    .build()
            ).collect(Collectors.toList());
            item.setEffects(effects);
        }

        return itemTemplateRepository.save(item);
    }

    @Transactional
    public ItemTemplate updateItem(Long id, ItemTemplateRequestDto dto, MultipartFile imageFile) {
        ItemTemplate item = itemTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item-ul nu a fost găsit cu ID: " + id));

        // 1. Actualizare câmpuri simple
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setSlot(dto.getSlot());
        item.setRarity(dto.getRarity());
        item.setMinLevel(dto.getMinLevel() != null ? dto.getMinLevel() : 1);
        item.setBuyPrice(dto.getBuyPrice());
        item.setSellPrice(dto.getSellPrice());
        item.setActive(dto.isActive());

        // 2. Actualizare Cerințe Atribute (Stats)
        item.setReqStr(dto.getReqStr() != null ? dto.getReqStr() : 0);
        item.setReqDex(dto.getReqDex() != null ? dto.getReqDex() : 0);
        item.setReqInt(dto.getReqInt() != null ? dto.getReqInt() : 0);
        item.setReqCha(dto.getReqCha() != null ? dto.getReqCha() : 0);

        // 3. Actualizare Imagine (Doar dacă se trimite un fișier nou)
        if (imageFile != null && !imageFile.isEmpty()) {
            // Opțional: Poți șterge imaginea veche de pe Cloudinary aici dacă vrei să faci curat
            // fileStorageService.deleteFile(item.getImageUrl());

            String newPublicId = fileStorageService.storeFile(imageFile);
            item.setImageUrl(newPublicId);
        }

        // 4. Actualizare Efecte (Ștergere vechi + Adăugare noi)
        // Folosim mecanismul orphanRemoval din Hibernate
        if (dto.getEffects() != null) {
            // A. Golim lista actuală
            item.getEffects().clear();

            // B. Construim lista nouă
            List<ItemEffect> newEffects = dto.getEffects().stream()
                    .map(eDto -> ItemEffect.builder()
                            .itemTemplate(item) // Setăm părintele
                            .effectType(eDto.getEffectType())
                            .value(eDto.getValue())
                            .targetCategory(eDto.getTargetCategory())
                            .build()
                    ).collect(Collectors.toList());

            // C. Adăugăm noile efecte în colecția persistentă
            item.getEffects().addAll(newEffects);
        } else {
            // Dacă nu se trimit efecte, le ștergem pe toate
            item.getEffects().clear();
        }

        return itemTemplateRepository.save(item);
    }

    // --- SHOP: Cumpărare Item ---
    @Transactional
    public void purchaseItem(User user, Long itemTemplateId) {
        ItemTemplate template = itemTemplateRepository.findById(itemTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Item-ul nu există."));

        // Verificări standard
        if (!template.isActive()) {
            throw new IllegalStateException("Acest item nu mai este disponibil.");
        }

        // --- 2. CALCUL PREȚ FINAL (CU DISCOUNT) ---
        // Verificăm ce discount are userul (de la alte iteme sau clasă)
        double totalDiscountPercent = bonusService.calculateFlatBonus(user, ItemEffectType.SHOP_DISCOUNT_GLOBAL);

        // Limităm la 90%
        if (totalDiscountPercent > 90) totalDiscountPercent = 90;

        // Calculăm prețul redus
        int originalPrice = template.getBuyPrice();
        int finalPrice = (int) Math.round(originalPrice * (1 - (totalDiscountPercent / 100.0)));
        if (finalPrice < 0) finalPrice = 0;

        // --- 3. VERIFICARE FONDURI PE PREȚUL REDUS ---
        if (user.getCoins() < finalPrice) {
            throw new IllegalStateException("Fonduri insuficiente. Preț necesar: " + finalPrice);
        }

        // --- 4. PROCESARE TRANZACȚIE ---
        user.setCoins(user.getCoins() - finalPrice);
        userRepository.save(user);

        // Construim descrierea pentru istoric
        String description = "Cumpărat item: " + template.getName();
        if (finalPrice < originalPrice) {
            description += String.format(" (Redus: %d -> %d)", originalPrice, finalPrice);
        }

        CoinTransaction transaction = CoinTransaction.builder()
                .user(user)
                .amount(-finalPrice) // Scădem suma redusă
                .description(description)
                .transactionType("ITEM_PURCHASE")
                .createdAt(LocalDateTime.now())
                .build();
        coinTransactionRepository.save(transaction);

        // 5. Adăugare în inventar
        UserItem newItem = UserItem.builder()
                .user(user)
                .itemTemplate(template)
                .isEquipped(false)
                .build();
        userItemRepository.save(newItem);
    }


    // --- INVENTORY: Echipare Item ---
    @Transactional
    public void equipItem(User user, Long userItemId) {
        UserItem itemToEquip = userItemRepository.findById(userItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item-ul nu există în inventarul tău."));

        if (!itemToEquip.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Acest item nu îți aparține.");
        }

        // Verifică nivelul necesar (dacă ai implementat un LevelService, apelează-l aici)
        // int userLevel = levelService.calculateLevel(user.getExperience()).getLevel();
        // if (userLevel < itemToEquip.getItemTemplate().getMinLevel()) ...
        ItemTemplate template = itemToEquip.getItemTemplate();

        // --- VERIFICARE CERINȚE ATRIBUTE ---
        if (user.getStrength() < template.getReqStr()) {
            throw new IllegalStateException("Ești prea slab! Necesită STR: " + template.getReqStr());
        }
        if (user.getDexterity() < template.getReqDex()) {
            throw new IllegalStateException("Nu ești destul de rapid! Necesită DEX: " + template.getReqDex());
        }
        if (user.getIntelligence() < template.getReqInt()) {
            throw new IllegalStateException("Nu ești destul de deștept! Necesită INT: " + template.getReqInt());
        }
        if (user.getCharisma() < template.getReqCha()) {
            throw new IllegalStateException("Nu ești destul de carismatic! Necesită CHA: " + template.getReqCha());
        }

        // 1. Găsește ce are userul echipat pe acel slot și dă-l jos (Unequip)
        ItemSlot slot = itemToEquip.getItemTemplate().getSlot();
        userItemRepository.findEquippedItemBySlot(user, slot).ifPresent(currentlyEquipped -> {
            currentlyEquipped.setEquipped(false);
            userItemRepository.save(currentlyEquipped);
        });

        // 2. Echipează noul item
        itemToEquip.setEquipped(true);
        userItemRepository.save(itemToEquip);
    }

    // --- INVENTORY: Dezechipare Item ---
    @Transactional
    public void unequipItem(User user, Long userItemId) {
        UserItem item = userItemRepository.findById(userItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item-ul nu există."));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Acest item nu îți aparține.");
        }

        item.setEquipped(false);
        userItemRepository.save(item);
    }

    // --- GET: Inventarul Userului ---
    public List<UserItemDto> getUserInventory(User user) {
        List<UserItem> items = userItemRepository.findByUser(user);
        // Procesăm fiecare item pentru a genera URL-ul complet al imaginii
        items.forEach(item -> {
            if (item.getItemTemplate().getImageUrl() != null && !item.getItemTemplate().getImageUrl().startsWith("http")) {
                item.getItemTemplate().setImageUrl(fileStorageService.getImageUrlFromPublicId(item.getItemTemplate().getImageUrl()));
            }
        });
        return items.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // Helper Mapper

    public UserItemDto mapToDto(UserItem userItem) {
        UserItemDto dto = new UserItemDto();
        dto.setId(userItem.getId());
        dto.setTemplateId(userItem.getItemTemplate().getId());
        dto.setName(userItem.getItemTemplate().getName());
        dto.setDescription(userItem.getItemTemplate().getDescription());
        // Aici va trebui sa injectezi FileStorageService daca vrei URL-ul complet, sau returnezi direct ID-ul
        dto.setImageUrl(userItem.getItemTemplate().getImageUrl()); 
        dto.setSlot(userItem.getItemTemplate().getSlot());
        dto.setRarity(userItem.getItemTemplate().getRarity());
        dto.setEquipped(userItem.isEquipped());
        dto.setMinLevel(userItem.getItemTemplate().getMinLevel());
        dto.setReqStr(userItem.getItemTemplate().getReqStr());
        dto.setReqDex(userItem.getItemTemplate().getReqDex());
        dto.setReqInt(userItem.getItemTemplate().getReqInt());
        dto.setReqCha(userItem.getItemTemplate().getReqCha());
        // Mapăm efectele pentru a le afișa în tooltip
        List<ItemEffectDto> effectDtos = userItem.getItemTemplate().getEffects().stream()
            .map(eff -> {
                ItemEffectDto eDto = new ItemEffectDto();
                eDto.setEffectType(eff.getEffectType());
                eDto.setValue(eff.getValue());
                eDto.setTargetCategory(eff.getTargetCategory());
                return eDto;
            }).collect(Collectors.toList());
        dto.setEffects(effectDtos);
        
        return dto;
    }
}