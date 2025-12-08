package com.example.UPIPaymentSystem.Controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.UPIPaymentSystem.AuthenticationServices.PDFBoxService;
import com.example.UPIPaymentSystem.Repo_Service.BankAccountService; // or your user helper service

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class StatementController
{
    private final PDFBoxService statementService;
    private final BankAccountService bankAccountService; // used to get authenticated user

    public StatementController(PDFBoxService statementService,
                               BankAccountService bankAccountService)
    {
        this.statementService = statementService;
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/statement/download")
    public void downloadStatement(
            @RequestParam(value = "duration", required = false, defaultValue = "1") int months,
            HttpServletResponse response) throws IOException {

        var user = bankAccountService.getUserFromAuthentication();
        String mobile = user.getMobile();

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months);

        byte[] pdfBytes = statementService.generateStatementForPeriod(mobile, start, end);

        String fileName = "statement_" + mobile + "_" + System.currentTimeMillis() + ".pdf";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
    }
}
