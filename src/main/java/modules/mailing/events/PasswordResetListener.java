package modules.mailing.events;

import EventBus.EventType;
import EventBus.Listener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import modules.mailing.DTO.EventOtpDTO;
import modules.mailing.Services.MailingService;

public class PasswordResetListener implements Listener {
    ObjectMapper mapper = new ObjectMapper();


    @Override
    public void invokeListener(EventType eventType, String message){
        EventOtpDTO otpDTO = new EventOtpDTO();
        try{
            otpDTO = mapper.readValue(message, EventOtpDTO.class);
        }
        catch(JsonProcessingException e){
            e.printStackTrace();
        }

        String messageBody = buildResetPasswordEmailBody(otpDTO.getOtp(), otpDTO.getMailTo());
                String subject = "Password Reset";
        new MailingService().sendMail(otpDTO.getMailTo(),subject,messageBody);
    }

    private String buildResetPasswordEmailBody(String otpCode, String recipientEmail){
        return String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "  <title>Password Reset OTP</title>\n" +
                        "  <style>\n" +
                        "    @media (prefers-color-scheme: dark) {\n" +
                        "      .dark-bg    { background-color: #14171f !important; }\n" +
                        "      .dark-card  { background-color: #1e222d !important; border-color: #2d3140 !important; }\n" +
                        "      .dark-text  { color: #f0f2f5 !important; }\n" +
                        "      .dark-sub   { color: #a8b2c9 !important; }\n" +
                        "      .dark-muted { color: #8a94a6 !important; }\n" +
                        "      .dark-otp   { background-color: #282d3c !important; border-color: #3b4155 !important; }\n" +
                        "      .dark-code  { color: #ffffff !important; }\n" +
                        "      .dark-hint  { color: #9aa3b8 !important; }\n" +
                        "      .dark-div   { border-color: #2d3140 !important; }\n" +
                        "      .dark-btn   { background-color: #6366f1 !important; }\n" +
                        "      .dark-foot  { color: #6b7280 !important; }\n" +
                        "      .dark-msg   { color: #d1d5db !important; }\n" +
                        "      .dark-warn-text { color: #fcd34d !important; }\n" +
                        "      .dark-warn-strong { color: #fde68a !important; }\n" +
                        "    }\n" +
                        "  </style>\n" +
                        "</head>\n" +
                        "<body style=\"margin:0; padding:20px; background-color:#f6f9fc; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">\n" +
                        "\n" +
                        "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" style=\"background-color:#f6f9fc; margin:0 auto;\">\n" +
                        "  <tr>\n" +
                        "    <td align=\"center\" style=\"padding:20px 0;\">\n" +
                        "      <table width=\"100%\" max-width=\"480\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" style=\"max-width:480px; width:100%; background-color:#ffffff; border-radius:24px; border:1px solid #eef2f6; box-shadow:0 16px 48px rgba(0,0,0,0.05);\">\n" +
                        "        <tr>\n" +
                        "          <td style=\"padding:40px 36px 32px;\" class=\"dark-bg dark-card\">\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding-bottom:28px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"width:40px; height:40px; background:linear-gradient(135deg,#4f46e5,#7c3aed); border-radius:10px; text-align:center; vertical-align:middle; color:#fff; font-weight:700; font-size:18px; letter-spacing:-0.3px;\">SP</td>\n" +
                        "                      <td style=\"padding-left:10px; font-size:20px; font-weight:700; color:#111827; letter-spacing:-0.3px;\" class=\"dark-text\">Secure<span style=\"color:#4f46e5;\">Pass</span></td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <h1 style=\"font-size:26px; font-weight:700; color:#111827; margin:0 0 6px 0; letter-spacing:-0.3px;\" class=\"dark-text\">Reset password</h1>\n" +
                        "            <p style=\"font-size:15px; color:#6b7280; margin:0 0 28px 0; line-height:1.5;\" class=\"dark-sub\">Enter the code below to verify your identity.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f8fafc; border-radius:14px; border:2px dashed #e2e8f0; margin-bottom:28px;\" class=\"dark-otp\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:28px 16px; text-align:center;\">\n" +
                        "                  <div style=\"font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:1.2px; color:#6b7280; margin-bottom:10px;\" class=\"dark-muted\">verification code</div>\n" +
                        "                  <div style=\"font-size:48px; font-weight:700; letter-spacing:10px; color:#111827; font-family:'SF Mono', 'Menlo', 'Consolas', monospace;\" class=\"dark-code\">%s</div>\n" +
                        "                  <div style=\"font-size:13px; color:#9ca3af; margin-top:12px;\" class=\"dark-hint\">expires in <span style=\"font-weight:600; color:#4f46e5;\">5 minutes</span></div>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <p style=\"font-size:15px; color:#374151; line-height:1.7; margin:0 0 8px 0;\" class=\"dark-msg\">We received a request to reset the password for your account associated with <strong style=\"color:#111827;\" class=\"dark-text\">%s</strong>.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#fffbeb; border-left:4px solid #f59e0b; border-radius:8px; margin:20px 0 28px 0;\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:16px 18px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"font-size:20px; vertical-align:top; padding-right:12px;\">⚠\uFE0F</td>\n" +
                        "                      <td style=\"font-size:14px; color:#78350f; line-height:1.5;\" class=\"dark-warn-text\"><strong style=\"color:#451a03;\" class=\"dark-warn-strong\">Didn't request this?</strong> &nbsp;Please ignore this email. Your password won't change unless you enter the code above and create a new one.</td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "              <tr>\n" +
                        "                <td align=\"center\">\n" +
                        "                  <a href=\"#\" style=\"display:block; background-color:#4f46e5; color:#ffffff !important; font-weight:600; font-size:15px; padding:13px 20px; border-radius:12px; text-decoration:none; text-align:center;\" class=\"dark-btn\">Reset Password</a>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:24px 0 16px;\">\n" +
                        "              <tr><td style=\"height:1px; background:#eef2f6;\" class=\"dark-div\"></td></tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "              <tr>\n" +
                        "                <td align=\"center\" style=\"font-size:12px; color:#9ca3af;\" class=\"dark-foot\">SecurePass &bull; 2026</td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "          </td>\n" +
                        "        </tr>\n" +
                        "      </table>\n" +
                        "    </td>\n" +
                        "  </tr>\n" +
                        "</table>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>",
                otpCode,
                recipientEmail
        );
    }

}
