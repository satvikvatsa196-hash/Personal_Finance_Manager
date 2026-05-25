package com.syfe.finance.controller;

import com.syfe.finance.model.SavingsGoal;
import com.syfe.finance.service.GoalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(Authentication authentication, @Valid @RequestBody CreateGoalRequest request) {
        SavingsGoal goal = goalService.create(authentication.getName(), request.goalName(), request.targetAmount(), request.targetDate(), request.startDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(authentication.getName(), goal));
    }

    @GetMapping
    public Map<String, List<GoalResponse>> list(Authentication authentication) {
        return Map.of("goals", goalService.list(authentication.getName()).stream()
                .map(goal -> toResponse(authentication.getName(), goal))
                .toList());
    }

    @GetMapping("/{id}")
    public GoalResponse get(Authentication authentication, @PathVariable long id) {
        return toResponse(authentication.getName(), goalService.get(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public GoalResponse update(Authentication authentication, @PathVariable long id, @Valid @RequestBody UpdateGoalRequest request) {
        return toResponse(authentication.getName(), goalService.update(authentication.getName(), id, request.targetAmount(), request.targetDate()));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(Authentication authentication, @PathVariable long id) {
        goalService.delete(authentication.getName(), id);
        return Map.of("message", "Goal deleted successfully");
    }

    private GoalResponse toResponse(String username, SavingsGoal goal) {
        GoalService.GoalProgress progress = goalService.progress(username, goal);
        return new GoalResponse(goal.id(), goal.goalName(), goal.targetAmount(), goal.targetDate(), goal.startDate(),
                progress.currentProgress(), progress.progressPercentage(), progress.remainingAmount());
    }

    public record CreateGoalRequest(
            @NotBlank String goalName,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal targetAmount,
            @NotNull LocalDate targetDate,
            LocalDate startDate
    ) {
    }

    public record UpdateGoalRequest(
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal targetAmount,
            LocalDate targetDate
    ) {
    }

    public record GoalResponse(
            long id,
            String goalName,
            BigDecimal targetAmount,
            LocalDate targetDate,
            LocalDate startDate,
            BigDecimal currentProgress,
            BigDecimal progressPercentage,
            BigDecimal remainingAmount
    ) {
    }
}
