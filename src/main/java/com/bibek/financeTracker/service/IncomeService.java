package com.bibek.financeTracker.service;

import com.bibek.financeTracker.dto.ExpenseDto;
import com.bibek.financeTracker.dto.IncomeDto;
import com.bibek.financeTracker.entity.CategoryEntity;
import com.bibek.financeTracker.entity.ExpenseEntity;
import com.bibek.financeTracker.entity.IncomeEntity;
import com.bibek.financeTracker.entity.ProfileEntity;
import com.bibek.financeTracker.repository.CategoryRepository;
import com.bibek.financeTracker.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;

    private final IncomeRepository  incomeRepository;

    private final ProfileService profileService;


    public IncomeDto addIncome(IncomeDto dto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(()-> new RuntimeException
                        ("Category not found with id: "+ dto.getCategoryId()));
        IncomeEntity newExpense = toEntity(dto, profile, category);
        newExpense = incomeRepository.save(newExpense);
        return toDTO(newExpense);
    }

    public void deleteIncome(Long incomeId){
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity expense = incomeRepository.findById(incomeId)
                .orElseThrow(()-> new RuntimeException
                        ("Income not found with id: "+ incomeId));
        if(!expense.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(expense);
    }


    //Retrieve all exprenses for the based on the start and end date
    public List<IncomeDto> getCurrentMonthIncomesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> expenses = incomeRepository.findByProfileIdAndDateBetween
                (profile.getId(), startDate, endDate);
        return expenses.stream().map(this::toDTO).toList();
    }



    private IncomeEntity toEntity(IncomeDto dto, ProfileEntity profile
            , CategoryEntity category) {
        return IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();

    }

    private IncomeDto toDTO(IncomeEntity entity) {
        return IncomeDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory()!= null
                        ? entity.getCategory().getId(): null)
                .categoryName(entity.getCategory()!=null
                        ? entity.getCategory().getName():null)
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
