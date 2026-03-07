package com.bibek.financeTracker.controller;


import com.bibek.financeTracker.dto.ExpenseDto;
import com.bibek.financeTracker.dto.IncomeDto;
import com.bibek.financeTracker.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;


    @PostMapping
    public ResponseEntity<IncomeDto> addExpense(@RequestBody IncomeDto dto){
        IncomeDto createdExpense = incomeService.addIncome(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getExpenses(){
        List<IncomeDto> expenses = incomeService.getCurrentMonthIncomesForCurrentUser();
        return ResponseEntity.ok(expenses);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id){
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }


}
