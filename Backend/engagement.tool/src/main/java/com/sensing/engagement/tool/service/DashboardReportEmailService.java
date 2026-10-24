package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.dto.hr.DashboardResponse;
import com.sensing.engagement.tool.dto.hr.ThemeMetricResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardReportEmailService {

    private final JavaMailSender mailSender;
    private final HrDashboardService hrDashboardService;

    @Value("${app.reports.recipient:}")
    private String configuredRecipient;

    public void sendDashboardReport() {
        String recipient = configuredRecipient == null ? "" : configuredRecipient.trim();
        if (recipient.isBlank()) {
            throw new IllegalStateException(
                    "Dashboard report recipient is not configured. Set app.reports.recipient in application.yaml."
            );
        }

        DashboardResponse dashboard = hrDashboardService.getDashboard();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject("Engagement Sensing | HR Dashboard Report");
            helper.setText(buildHtmlReport(dashboard), true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to send the dashboard report email. Check SMTP configuration and recipient address.", e);
        }
    }

    private String buildHtmlReport(DashboardResponse data) {
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        List<ThemeMetricResponse> themes = data.getThemeMetrics() == null ? List.of() : data.getThemeMetrics();
        StringBuilder themeRows = new StringBuilder();

        if (themes.isEmpty()) {
            themeRows.append("<tr><td style='padding:16px;color:#667085;border-bottom:1px solid #eaecf0;'>No scored responses are available yet.</td></tr>");        } else {
            for (ThemeMetricResponse theme : themes) {
                String name = escapeHtml(theme.getTheme() == null ? "Uncategorized" : theme.getTheme());
                double score = theme.getScore() == null ? 0.0 : theme.getScore();
                double percentage = Math.max(0, Math.min(100, score / 5.0 * 100));
                String scoreColor = score < 3 ? "#b42318" : score < 4 ? "#b54708" : "#027a48";
                themeRows.append("""
                    <tr>
                      <td style="padding:14px 12px;border-bottom:1px solid #eaecf0;font-size:14px;color:#344054;width:34%%;">%s</td>
                      <td style="padding:14px 12px;border-bottom:1px solid #eaecf0;width:48%%;">
                        <div style="height:8px;background:#f2f4f7;border-radius:8px;overflow:hidden;">
                          <div style="height:8px;width:%.1f%%;background:%s;border-radius:8px;"></div>
                        </div>
                      </td>
                      <td style="padding:14px 12px;border-bottom:1px solid #eaecf0;text-align:right;font-size:14px;font-weight:700;color:%s;white-space:nowrap;">%.2f / 5</td>
                    </tr>
                    """.formatted(name, percentage, scoreColor, scoreColor, score));
            }
        }

        double engagement = data.getOverallEngagementScore() == null ? 0 : data.getOverallEngagementScore();
        double responseRate = data.getResponseRate() == null ? 0 : data.getResponseRate();
        long totalEmployees = data.getTotalEmployees() == null ? 0 : data.getTotalEmployees();
        long completed = data.getCompletedSurveys() == null ? 0 : data.getCompletedSurveys();
        long pending = data.getPendingSurveys() == null ? 0 : data.getPendingSurveys();
        long totalSurveys = data.getTotalSurveys() == null ? 0 : data.getTotalSurveys();

        String insight;
        if (themes.isEmpty()) {
            insight = "Theme-level insights will appear once scored responses are available.";
        } else {
            ThemeMetricResponse lowest = themes.stream().min((a, b) -> Double.compare(safeScore(a), safeScore(b))).orElse(null);
            ThemeMetricResponse highest = themes.stream().max((a, b) -> Double.compare(safeScore(a), safeScore(b))).orElse(null);
            if (lowest == highest || lowest == null) {
                insight = "Continue monitoring theme scores as more responses are collected.";
            } else {
                insight = "Highest-scoring theme: <strong>" + escapeHtml(highest.getTheme() == null ? "Uncategorized" : highest.getTheme()) + "</strong> (" + String.format("%.2f", safeScore(highest)) + "/5). "
                        + "Theme to review: <strong>" + escapeHtml(lowest.getTheme() == null ? "Uncategorized" : lowest.getTheme()) + "</strong> (" + String.format("%.2f", safeScore(lowest)) + "/5).";
            }
        }

        return """
            <!doctype html>
            <html><body style="margin:0;padding:0;background:#f2f4f7;font-family:Arial,Helvetica,sans-serif;color:#101828;">
              <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="background:#f2f4f7;padding:28px 10px;">
                <tr><td align="center">
                  <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="680" style="max-width:680px;width:100%%;background:#ffffff;border-radius:12px;overflow:hidden;">
                    <tr><td style="background:#101828;padding:28px 32px;border-bottom:5px solid #e30613;">
                      <div style="font-size:12px;letter-spacing:1.6px;text-transform:uppercase;color:#d0d5dd;font-weight:700;">Engagement Sensing</div>
                      <div style="font-size:27px;line-height:1.25;color:#ffffff;font-weight:700;margin-top:8px;">HR Dashboard Report</div>
                      <div style="font-size:13px;color:#d0d5dd;margin-top:8px;">Generated %s</div>
                    </td></tr>
                    <tr><td style="padding:28px 32px 12px;">
                      <div style="font-size:18px;font-weight:700;color:#101828;">Executive snapshot</div>
                      <div style="font-size:13px;line-height:1.6;color:#667085;margin-top:6px;">A concise view of survey participation and employee sentiment based on the responses currently recorded in the system.</div>
                    </td></tr>
                    <tr><td style="padding:8px 24px 18px;">
                      <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%"><tr>
                        <td width="50%%" style="padding:8px;"><div style="background:#f9fafb;border:1px solid #eaecf0;border-radius:9px;padding:17px;"><div style="font-size:11px;color:#667085;text-transform:uppercase;letter-spacing:.5px;">Engagement score</div><div style="font-size:28px;font-weight:700;color:#101828;margin-top:8px;">%.2f <span style="font-size:13px;color:#667085;font-weight:400;">/ 5</span></div></div></td>
                        <td width="50%%" style="padding:8px;"><div style="background:#f9fafb;border:1px solid #eaecf0;border-radius:9px;padding:17px;"><div style="font-size:11px;color:#667085;text-transform:uppercase;letter-spacing:.5px;">Response rate</div><div style="font-size:28px;font-weight:700;color:#101828;margin-top:8px;">%.1f<span style="font-size:16px;">%%</span></div></div></td>
                      </tr><tr>
                        <td width="50%%" style="padding:8px;"><div style="background:#f9fafb;border:1px solid #eaecf0;border-radius:9px;padding:17px;"><div style="font-size:11px;color:#667085;text-transform:uppercase;letter-spacing:.5px;">Employees</div><div style="font-size:25px;font-weight:700;color:#101828;margin-top:8px;">%d</div></div></td>
                        <td width="50%%" style="padding:8px;"><div style="background:#f9fafb;border:1px solid #eaecf0;border-radius:9px;padding:17px;"><div style="font-size:11px;color:#667085;text-transform:uppercase;letter-spacing:.5px;">Surveys completed</div><div style="font-size:25px;font-weight:700;color:#101828;margin-top:8px;">%d <span style="font-size:13px;color:#667085;font-weight:400;">of %d</span></div><div style="font-size:12px;color:#667085;margin-top:5px;">%d pending</div></div></td>
                      </tr></table>
                    </td></tr>
                    <tr><td style="padding:8px 32px 10px;"><div style="font-size:18px;font-weight:700;color:#101828;">Theme performance</div><div style="font-size:13px;color:#667085;margin-top:5px;">Average score for each theme. Scores are out of 5.</div></td></tr>
                    <tr><td style="padding:4px 24px 22px;"><table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="border:1px solid #eaecf0;border-radius:8px;border-spacing:0;overflow:hidden;">
                      <tr style="background:#f9fafb;"><th align="left" style="padding:12px;font-size:11px;color:#667085;text-transform:uppercase;letter-spacing:.5px;">Theme</th><th align="left" style="padding:12px;font-size:11px;color:#667085;text-transform:uppercase;letter-spacing:.5px;">Score level</th><th align="right" style="padding:12px;font-size:11px;color:#667085;text-transform:uppercase;letter-spacing:.5px;">Score</th></tr>
                      %s
                    </table></td></tr>
                    <tr><td style="padding:0 32px 24px;"><div style="background:#fff8f1;border-left:4px solid #f79009;border-radius:6px;padding:16px 18px;"><div style="font-size:13px;font-weight:700;color:#93370d;margin-bottom:7px;">What to notice</div><div style="font-size:13px;line-height:1.65;color:#7a2e0e;">%s</div></div></td></tr>
                    <tr><td style="padding:18px 32px;background:#f9fafb;border-top:1px solid #eaecf0;"><div style="font-size:11px;line-height:1.6;color:#667085;">This report summarizes aggregate survey data currently available in Engagement Sensing. Scores describe submitted responses and should be interpreted alongside response volume and context. Please handle this report according to your organization's employee-data policies.</div></td></tr>
                  </table>
                </td></tr>
              </table>
            </body></html>
            """.formatted(generatedAt, engagement, responseRate, totalEmployees, completed, totalSurveys, pending, themeRows, insight);
    }

    private double safeScore(ThemeMetricResponse metric) {
        return metric.getScore() == null ? 0.0 : metric.getScore();
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
