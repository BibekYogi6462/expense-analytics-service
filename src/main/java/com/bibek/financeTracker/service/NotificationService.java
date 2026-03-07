package com.bibek.financeTracker.service;

import com.bibek.financeTracker.dto.ExpenseDto;
import com.bibek.financeTracker.entity.ProfileEntity;
import com.bibek.financeTracker.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    //    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kathmandu")
//    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kathmandu")
    public void sendDailyIncomeExpenseReminder(){
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for(ProfileEntity profile : profiles){
            try {
                String subject = "Daily Finance Tracker Reminder";
                String body = "Dear " + profile.getFullName() + ",\n\n" +
                        "This is your daily reminder to log your income and expenses in Finance Tracker. Keeping track of your finances can help you manage your budget and achieve your financial goals.\n\n" +
                        "Please click the link below to log in and update your financial records:\n\n" +
                        frontendUrl + "\n\n" +
                        "If you have already logged your income and expenses for today, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Finance Tracker Team";
                emailService.sendEmail(profile.getEmail(), subject, body);
            }catch (Exception e){
                log.error("Failed to send daily reminder email to profile with id: "+ profile.getId(), e);
            }
        }
    }

//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kathmandu")
    public void sendDailyExpenseSummaryEmail(){
        log.info("Job started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for(ProfileEntity profile : profiles){
            try {
                // FIXED: Use the corrected method that accepts profileId
                List<ExpenseDto> todayExpenses = expenseService.getExpensesForUserOnDate(
                        profile.getId(),  // Pass the profile ID directly
                        LocalDate.now(ZoneId.of("Asia/Kathmandu"))
                );

                if(!todayExpenses.isEmpty()) {
                    StringBuilder table = new StringBuilder();
                    table.append("<table style='border-collapse: collapse; width: 100%;'>");
                    table.append("<tr><th style='border: 1px solid #ddd; padding: 8px;'>Name</th>")
                            .append("<th style='border: 1px solid #ddd; padding: 8px;'>Amount</th>")
                            .append("<th style='border: 1px solid #ddd; padding: 8px;'>Category</th>")
                            .append("<th style='border: 1px solid #ddd; padding: 8px;'>Date</th></tr>");

                    for(ExpenseDto expense : todayExpenses){
                        table.append("<tr>")
                                .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expense.getName()).append("</td>")
                                .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expense.getAmount()).append("</td>")
                                .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expense.getCategoryName()).append("</td>")
                                .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expense.getDate()).append("</td>")
                                .append("</tr>");
                    }
                    table.append("</table>");

                    String body = "Hi " + profile.getFullName() + ",<br><br>" +
                            "Here is a summary of your expenses for today:<br><br>" +
                            table.toString() + "<br><br>" +
                            "Keep tracking your expenses to manage your finances better!<br><br>" +
                            "Best regards,<br>" +
                            "Finance Tracker Team";

                    emailService.sendEmail(profile.getEmail(), "Your Daily Expense Summary", body);
                    log.info("Expense summary sent successfully to: {}", profile.getEmail());
                }
            } catch (Exception e) {
                log.error("Failed to send daily expense summary email to profile with id: " + profile.getId(), e);
            }
        }
        log.info("Job completed: sendDailyExpenseSummary()");
    }
}