package com.bibek.financeTracker.service;

import com.bibek.financeTracker.dto.ExpenseDto;
import com.bibek.financeTracker.entity.CategoryEntity;
import com.bibek.financeTracker.entity.ExpenseEntity;
import com.bibek.financeTracker.entity.ProfileEntity;
import com.bibek.financeTracker.repository.CategoryRepository;
import com.bibek.financeTracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public ExpenseDto addExpense(ExpenseDto dto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(()-> new RuntimeException
                        ("Category not found with id: "+ dto.getCategoryId()));
        ExpenseEntity newExpense = toEntity(dto, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    //Retrieve all exprenses for the based on the start and end date
    public List<ExpenseDto> getCurrentMonthExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween
                (profile.getId(), startDate, endDate);
        return expenses.stream().map(this::toDTO).toList();
    }

    //Delete Expense by id
    public void deleteExpense(Long expenseId){
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(()-> new RuntimeException
                        ("Expense not found with id: "+ expenseId));
        if(!expense.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    //Get latest 5 expenses for the current user
    public List<ExpenseDto> getLatest5ExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream().map(this::toDTO).toList();
    }

    //Get total expenses for the current user
    public BigDecimal getTotalExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total =  expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    //filter messages
    public List<ExpenseDto> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),
                        startDate, endDate, keyword, sort);
        return expenses.stream().map(this::toDTO).toList();
    }

    // FIXED: For scheduled tasks - uses the provided profileId instead of security context
    public List<ExpenseDto> getExpensesForUserOnDate(Long profileId, LocalDate date){
        // Use the provided profileId directly without trying to get current profile
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenses.stream().map(this::toDTO).toList();
    }

    // Overloaded method for backward compatibility with controllers
    public List<ExpenseDto> getExpensesForCurrentUserOnDate(LocalDate date){
        ProfileEntity profile = profileService.getCurrentProfile();
        return getExpensesForUserOnDate(profile.getId(), date);
    }

    private ExpenseEntity toEntity(ExpenseDto dto, ProfileEntity profile
            , CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDto toDTO(ExpenseEntity entity) {
        return ExpenseDto.builder()
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