package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.enums.EmailStatus;
import com.sensing.engagement.tool.model.EmailNotification;
import com.sensing.engagement.tool.model.Survey;
import com.sensing.engagement.tool.repository.EmailNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.sensing.engagement.tool.enums.EmailStatus.SENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final EmailNotificationRepository emailNotificationRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendSurveyInvitation(Survey survey) {

        String recipientEmail = survey.getUser().getEmail();

        EmailNotification notification = EmailNotification.builder()
                .survey(survey)
                .email(recipientEmail)
                .status(String.valueOf(EmailStatus.PENDING))
                .build();

        notification = emailNotificationRepository.save(notification);

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(recipientEmail);
            message.setSubject("Employee Engagement Survey");
            message.setText(buildEmailBody(survey));

            mailSender.send(message);

            notification.setStatus(String.valueOf(EmailStatus.SENT));
            log.info("Email to user having email id as: {}", recipientEmail);
            notification.setSentAt(LocalDateTime.now());

        } catch (Exception e) {

            notification.setStatus(String.valueOf(EmailStatus.FAILED));

            log.error("Failed to send survey email to {}: {}", recipientEmail, e.getMessage());
        }

        emailNotificationRepository.save(notification);
    }

    private String buildEmailBody(Survey survey) {

        String employeeName = survey.getUser().getName();

        String surveyLink = frontendUrl
                        + "/survey/"
                        + survey.getId()
                        + "?employeeId="
                        + survey.getUser().getEmployeeId();

        return """
                Hello %s,

                Your Employee Engagement Survey is now available.

                We value your feedback and would appreciate it if you
                could take a few minutes to complete the survey.

                Click the link below to view and start your survey:

                %s

                Thank you,
                HR Team
                """.formatted(employeeName, surveyLink);
    }
}