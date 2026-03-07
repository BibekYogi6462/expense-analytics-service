package com.bibek.financeTracker.service;

import com.bibek.financeTracker.dto.CategoryDto;
import com.bibek.financeTracker.entity.CategoryEntity;
import com.bibek.financeTracker.entity.ProfileEntity;
import com.bibek.financeTracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    public CategoryDto saveCategory(CategoryDto categoryDto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(categoryDto.getName(), profile.getId())) {
            throw new RuntimeException("Category name already exists for this profile");
        }
        CategoryEntity categoryEntity = toEntity(categoryDto, profile);
        categoryEntity = categoryRepository.save(categoryEntity);
        return toDTO(categoryEntity);
    }

    //get categories for current profile
    public List<CategoryDto> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();

    }

    public List<CategoryDto> getCategoriesByTypeForCurrentUser(
             String type) {
        ProfileEntity profile= profileService.getCurrentProfile();
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return entities.stream().map(this::toDTO).toList();

    }


    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity categoryEntity = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found for this profile"));
        if (!categoryEntity.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByNameAndProfileId(categoryDto.getName(), profile.getId())) {
            throw new RuntimeException("Category name already exists for this profile");
        }
        categoryEntity.setName(categoryDto.getName());
        categoryEntity.setIcon(categoryDto.getIcon());
        categoryEntity.setType(categoryDto.getType());
        categoryEntity = categoryRepository.save(categoryEntity);
        return toDTO(categoryEntity);
    }





    //helper method to check if category name already exists for the profile
    private CategoryEntity toEntity(CategoryDto categoryDto, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(categoryDto.getName())
                .icon(categoryDto.getIcon())
                .profile(profile)
                .type(categoryDto.getType())
                .build();
    }

    private CategoryDto toDTO(CategoryEntity categoryEntity) {
        return CategoryDto.builder()
                .id(categoryEntity.getId())
                .profileId(categoryEntity.getProfile()!=null? categoryEntity.getProfile().getId(): null)
                .name(categoryEntity.getName())
                .icon(categoryEntity.getIcon())
                .createdAt(categoryEntity.getCreatedAt())
                .updatedAt(categoryEntity.getUpdatedAt())
                .type(categoryEntity.getType())
                .build();
    }



}
