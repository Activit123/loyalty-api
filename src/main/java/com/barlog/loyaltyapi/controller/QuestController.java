package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.QuestCreateDto;
import com.barlog.loyaltyapi.dto.QuestDetailsDto;
import com.barlog.loyaltyapi.dto.UserQuestLogDto;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.UserRepository;
import com.barlog.loyaltyapi.service.QuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestController {
    
    private final QuestService questService;

    // --- 1. Admin: Creare/Management Quest-uri ---
    private final UserRepository userRepository; // 2. INJECTARE DIRECTĂ REPO

    // === 3. LOGICA SOLICITATĂ ÎN CONTROLLER ===
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/quests/sync-users")
    public ResponseEntity<String> syncQuestsToAllUsers() {
        // A. Luăm toți userii direct din baza de date (aici în controller)
        List<User> allUsers = userRepository.findAll();

        int count = 0;
        // B. Iterăm prin listă (logică de distribuire în controller)
        for (User user : allUsers) {
            // C. Apelăm metoda de asignare per user (care face verificările de duplicat)
            questService.assignActiveQuests(user);
            count++;
        }

        return ResponseEntity.ok("Sincronizare efectuată: Quest-urile au fost verificate și distribuite pentru " + count + " utilizatori.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/quests")
    public ResponseEntity<?> createQuest(@Valid @RequestBody QuestCreateDto createDto) {
        // TODO: Implementează maparea DTO -> Entitate și logica de salvare în QuestService
         return new ResponseEntity<>(questService.createQuest(createDto), HttpStatus.CREATED);

    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/quests")
    public ResponseEntity<List<QuestDetailsDto>> getAllQuestsForAdmin() {
        List<QuestDetailsDto> allQuests = questService.getAllQuestsForAdmin();
        return ResponseEntity.ok(allQuests);
    }

    // --- 2. User: Vizualizare Log ---
    

    @GetMapping("/quests/log")
    public ResponseEntity<List<UserQuestLogDto>> getUserQuestLog(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        // TODO: Implementează maparea și logica de preluare a log-ului
       return ResponseEntity.ok(questService.getUserQuestLog(currentUser));

    }
    // NOU: Endpoint pentru Actualizare (Modificare) Quest
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/quests/{questId}")
    public ResponseEntity<QuestDetailsDto> updateQuest(
            @PathVariable Long questId,
            @Valid @RequestBody QuestCreateDto updateDto) {

        QuestDetailsDto questDetails = questService.updateQuest(questId, updateDto);
        return ResponseEntity.ok(questDetails);
    }
    // NOU: Endpoint pentru Ștergere/Dezactivare
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/quests/{questId}")
    public ResponseEntity<Void> deleteQuest(@PathVariable Long questId) {
        questService.deleteQuest(questId);
        return ResponseEntity.noContent().build();
    }


    // --- 3. User: Revendicare Recompensă ---

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/quests/log/claim/{logId}")
    public ResponseEntity<UserQuestLogDto> claimReward(Authentication authentication, @PathVariable Long logId) {
        User currentUser = (User) authentication.getPrincipal();
        // TODO: Implementează logica de revendicare
         return ResponseEntity.ok(questService.claimReward(currentUser, logId));

    }
}