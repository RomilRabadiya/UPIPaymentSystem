package com.example.UPIPaymentSystem.AuthenticationServices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import com.example.UPIPaymentSystem.Entity.Transaction;
import com.example.UPIPaymentSystem.Repo_Service.TransactionRepository;

@Service
public class PDFBoxService
{
    private final TransactionRepository transactionRepository;

    public PDFBoxService(TransactionRepository transactionRepository)
    {
        this.transactionRepository = transactionRepository;
    }

    public byte[] generateStatementForPeriod(String mobile, LocalDate start, LocalDate end) throws IOException
    {
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay(); // include last day

        List<Transaction> transactions = transactionRepository.findByUserMobileAndTimestampBetween(mobile, from, to);

        BigDecimal totalSent = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        int failedCount = 0;

        for (Transaction t : transactions)
        {
            if ("FAILED".equalsIgnoreCase(t.getStatus()))
                failedCount++;

            if (t.getFromAccount() != null &&
                t.getFromAccount().getUser() != null &&
                mobile.equals(t.getFromAccount().getUser().getMobile()))
            {
                totalSent = totalSent.add(t.getAmount());
            }

            if (t.getToAccount() != null &&
                t.getToAccount().getUser() != null &&
                mobile.equals(t.getToAccount().getUser().getMobile()))
            {
                totalReceived = totalReceived.add(t.getAmount());
            }
        }

        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            // TITLE
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
            cs.newLineAtOffset(50, 740);
            cs.showText("Transaction Statement");
            cs.endText();

            // PERIOD
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(50, 720);
            cs.showText("Period: " + start.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) +
                        " - " + end.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            cs.endText();

            // SUMMARY
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
            cs.newLineAtOffset(50, 690);
            cs.showText("Summary");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(60, 670);
            cs.showText("Total Sent: " + format(totalSent) + " INR");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(60, 655);
            cs.showText("Total Received: " + format(totalReceived) + " INR");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(60, 640);
            cs.showText("Failed Transactions: " + failedCount);
            cs.endText();

            // TRANSACTION TABLE HEADER
            float y = 610;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.newLineAtOffset(50, y);
            cs.showText("Time");
            cs.newLineAtOffset(100, 0);
            cs.showText("Description");
            cs.newLineAtOffset(200, 0);
            cs.showText("Status");
            cs.newLineAtOffset(100, 0);
            cs.showText("Amount");
            cs.endText();

            y -= 20;
            cs.setFont(PDType1Font.HELVETICA, 10);

            // TRANSACTION ROWS
            for (Transaction t : transactions)
            {
                if (y < 50)
                {
                    cs.close();
                    PDPage newPage = new PDPage(PDRectangle.LETTER);
                    doc.addPage(newPage);
                    cs = new PDPageContentStream(doc, newPage);
                    y = 700;
                }

                String time = t.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MM HH:mm"));
                String desc = t.getDescription() == null ? "" : truncate(t.getDescription(), 25);
                String status = t.getStatus();
                String amount = format(t.getAmount()) + " INR";

                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText(time);
                cs.newLineAtOffset(100, 0);
                cs.showText(desc);
                cs.newLineAtOffset(200, 0);
                cs.showText(status);
                cs.newLineAtOffset(100, 0);
                cs.showText(amount);
                cs.endText();

                y -= 18;
            }

            cs.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private String format(BigDecimal amt)
    {
        return amt.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private String truncate(String s, int len)
    {
        return s.length() <= len ? s : s.substring(0, len - 1) + "...";
    }
}