package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ClassTypeDto;
import com.barlog.loyaltyapi.dto.LevelInfoDto;
import com.barlog.loyaltyapi.dto.RaceDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.ClassType;
import com.barlog.loyaltyapi.model.Race;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.ClassTypeRepository;
import com.barlog.loyaltyapi.repository.RaceRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final UserRepository userRepository;
    private final RaceRepository raceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final LevelService levelService; // Avem nevoie de el pentru a verifica nivelul

    // --- Metode pentru a prelua informațiile ---

    public List<RaceDto> getAllRaces() {
        return raceRepository.findAll().stream()
                .map(this::mapToRaceDto)
                .collect(Collectors.toList());
    }

    public List<ClassTypeDto> getAllClassTypes() {
        return classTypeRepository.findAll().stream()
                .map(this::mapToClassTypeDto)
                .collect(Collectors.toList());
    }

    // --- Metode pentru a selecta rasa și clasa ---

    @Transactional
    public User selectRace(User currentUser, Long raceId) {
        if (currentUser.getRace() != null) {
            throw new IllegalStateException("Rasa a fost deja aleasă.");
        }
        Race selectedRace = raceRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Rasa nu există."));

        currentUser.setRace(selectedRace);

        // --- SETARE ATRIBUTE DE BAZĂ ---
        currentUser.setStrength(selectedRace.getBaseStr());
        currentUser.setDexterity(selectedRace.getBaseDex());
        currentUser.setIntelligence(selectedRace.getBaseInt());
        currentUser.setCharisma(selectedRace.getBaseCha());

        // --- CALCUL PUNCTE RETROACTIVE ---
        // Dacă userul are deja nivel mare (ex: lvl 5), primește punctele pentru nivelele 2,3,4,5
        LevelInfoDto levelInfo = levelService.calculateLevelInfo(currentUser.getExperience());
        int points = (levelInfo.getLevel() - 1) * 5;
        currentUser.setUnallocatedPoints(points);

        return userRepository.save(currentUser);
    }
    @Transactional
    public User selectClass(User currentUser, Long classId) {
        if (currentUser.getClassType() != null) {
            throw new IllegalStateException("Clasa a fost deja aleasă și nu poate fi schimbată.");
        }

        // Verificăm dacă utilizatorul are nivelul necesar
        int currentLevel = levelService.calculateLevelInfo(currentUser.getExperience()).getLevel();
        if (currentLevel < 10) {
            throw new IllegalStateException("Trebuie să ai cel puțin nivelul 10 pentru a alege o clasă.");
        }

        ClassType selectedClass = classTypeRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Clasa cu ID-ul " + classId + " nu a fost găsită."));

        currentUser.setClassType(selectedClass);
        return userRepository.save(currentUser);
    }

    // --- Metode ajutătoare de mapare (Mappers) ---

    protected RaceDto mapToRaceDto(Race race) {
        RaceDto dto = new RaceDto();
        dto.setId(race.getId());
        dto.setName(race.getName());
        dto.setDescription(race.getDescription());
        dto.setPrimaryAttribute(race.getPrimaryAttribute());
        dto.setRacialBenefit(race.getRacialBenefit());
        return dto;
    }

    protected ClassTypeDto mapToClassTypeDto(ClassType classType) {
        ClassTypeDto dto = new ClassTypeDto();
        dto.setId(classType.getId());
        dto.setName(classType.getName());
        dto.setDescription(classType.getDescription());
        dto.setRequiredAttribute(classType.getRequiredAttribute());
        return dto;
    }
}