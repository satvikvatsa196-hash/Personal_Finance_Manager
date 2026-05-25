package com.syfe.finance.controller;

import com.syfe.finance.exception.ApiException;
import com.syfe.finance.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public MonthlyReportResponse monthly(Authentication authentication, @PathVariable int year, @PathVariable int month) {
        if (month < 1 || month > 12) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Month must be between 1 and 12");
        }
        ReportService.ReportData data = reportService.monthly(authentication.getName(), year, month);
        return new MonthlyReportResponse(month, year, data.totalIncome(), data.totalExpenses(), data.netSavings());
    }

    @GetMapping("/yearly/{year}")
    public YearlyReportResponse yearly(Authentication authentication, @PathVariable int year) {
        ReportService.ReportData data = reportService.yearly(authentication.getName(), year);
        return new YearlyReportResponse(year, data.totalIncome(), data.totalExpenses(), data.netSavings());
    }

    public record MonthlyReportResponse(
            int month,
            int year,
            Map<String, BigDecimal> totalIncome,
            Map<String, BigDecimal> totalExpenses,
            BigDecimal netSavings
    ) {
    }

    public record YearlyReportResponse(
            int year,
            Map<String, BigDecimal> totalIncome,
            Map<String, BigDecimal> totalExpenses,
            BigDecimal netSavings
    ) {
    }
}
