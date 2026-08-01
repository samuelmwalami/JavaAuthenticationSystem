package modules.mailing.Services;

import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import utils.ConfigReaders.MailingConfigReader;

public class MailingService {
    MailingConfigReader mailingConfigReader = new MailingConfigReader();


    public void sendMail(String to, String subject, String messageBody){
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.host", mailingConfigReader.getMailHost());
        properties.setProperty("mail.smtp.port", "587");

        String userName = mailingConfigReader.getMailUserName();
        String password = mailingConfigReader.getMailPassword();

        IO.println("Setting up mail authenticator");
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(userName, password);
            }
        };

        IO.println("Setting up mail Session");
        Session session = Session.getInstance(properties,authenticator);

        try {
            IO.println("Preparing the mail");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailingConfigReader.getMailFrom()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);


            message.setContent(messageBody,"text/html");

            Transport.send(message);
            IO.println("Email has been sent");
        }
        catch(AddressException e){
            IO.println("An error occurred while sending the email");
            e.printStackTrace();
        }
        catch (MessagingException e){
            e.printStackTrace();
        }
    }

    public String buildVerifyRegistrationEmailBody(String otpCode, String recipientEmail) {
        String expiryText = "5 minutes";
        String template =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "  <title>Verify Your Email</title>\n" +
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
                        "            <h1 style=\"font-size:26px; font-weight:700; color:#111827; margin:0 0 6px 0; letter-spacing:-0.3px;\" class=\"dark-text\">Verify your email</h1>\n" +
                        "            <p style=\"font-size:15px; color:#6b7280; margin:0 0 28px 0; line-height:1.5;\" class=\"dark-sub\">Enter the code below to confirm your registration.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f8fafc; border-radius:14px; border:2px dashed #e2e8f0; margin-bottom:28px;\" class=\"dark-otp\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:28px 16px; text-align:center;\">\n" +
                        "                  <div style=\"font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:1.2px; color:#6b7280; margin-bottom:10px;\" class=\"dark-muted\">verification code</div>\n" +
                        "                  <div style=\"font-size:48px; font-weight:700; letter-spacing:10px; color:#111827; font-family:'SF Mono', 'Menlo', 'Consolas', monospace;\" class=\"dark-code\">%s</div>\n" +
                        "                  <div style=\"font-size:13px; color:#9ca3af; margin-top:12px;\" class=\"dark-hint\">expires in <span style=\"font-weight:600; color:#4f46e5;\">%s</span></div>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <p style=\"font-size:15px; color:#374151; line-height:1.7; margin:0 0 8px 0;\" class=\"dark-msg\">We received a registration request for <strong style=\"color:#111827;\" class=\"dark-text\">%s</strong>. Please verify your email address to activate your account.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#fffbeb; border-radius:8px; margin:20px 0 28px 0;\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:16px 18px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"font-size:20px; vertical-align:top; padding-right:12px;\"></td>\n" +
                        "                      <td style=\"font-size:14px; color:#78350f; line-height:1.5;\" class=\"dark-warn-text\"><strong style=\"color:#451a03;\" class=\"dark-warn-strong\">Didn't sign up?</strong> &nbsp;Please ignore this email. No changes will be made to your account.</td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "              <tr>\n" +
                        "                <td align=\"center\">\n" +
                        "                  <a href=\"#\" style=\"display:block; background-color:#4f46e5; color:#ffffff !important; font-weight:600; font-size:15px; padding:13px 20px; border-radius:12px; text-decoration:none; text-align:center;\" class=\"dark-btn\">Verify Email</a>\n" +
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
                        "</html>";

        template = template.replaceAll("%(?![s])", "%%");
        return String.format(template, otpCode, expiryText, recipientEmail);
    }

    public String buildResetPasswordEmailBody(String otpCode, String recipientEmail) {
        String expiryText = "5 minutes";
        String template =
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
                        "                  <div style=\"font-size:13px; color:#9ca3af; margin-top:12px;\" class=\"dark-hint\">expires in <span style=\"font-weight:600; color:#4f46e5;\">%s</span></div>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <p style=\"font-size:15px; color:#374151; line-height:1.7; margin:0 0 8px 0;\" class=\"dark-msg\">We received a request to reset the password for your account associated with <strong style=\"color:#111827;\" class=\"dark-text\">%s</strong>.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#fffbeb; border-radius:8px; margin:20px 0 28px 0;\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:16px 18px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"font-size:20px; vertical-align:top; padding-right:12px;\"></td>\n" +
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
                        "</html>";

        template = template.replaceAll("%(?![s])", "%%");
        return String.format(template, otpCode, expiryText, recipientEmail);
    }

    public String buildLoginVerificationEmailBody(String otpCode, String recipientEmail) {
        String expiryText = "5 minutes";
        String template =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "  <title>Login OTP</title>\n" +
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
                        "            <h1 style=\"font-size:26px; font-weight:700; color:#111827; margin:0 0 6px 0; letter-spacing:-0.3px;\" class=\"dark-text\">Verify your login</h1>\n" +
                        "            <p style=\"font-size:15px; color:#6b7280; margin:0 0 28px 0; line-height:1.5;\" class=\"dark-sub\">Enter the code below to complete your login.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f8fafc; border-radius:14px; border:2px dashed #e2e8f0; margin-bottom:28px;\" class=\"dark-otp\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:28px 16px; text-align:center;\">\n" +
                        "                  <div style=\"font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:1.2px; color:#6b7280; margin-bottom:10px;\" class=\"dark-muted\">verification code</div>\n" +
                        "                  <div style=\"font-size:48px; font-weight:700; letter-spacing:10px; color:#111827; font-family:'SF Mono', 'Menlo', 'Consolas', monospace;\" class=\"dark-code\">%s</div>\n" +
                        "                  <div style=\"font-size:13px; color:#9ca3af; margin-top:12px;\" class=\"dark-hint\">expires in <span style=\"font-weight:600; color:#4f46e5;\">%s</span></div>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <p style=\"font-size:15px; color:#374151; line-height:1.7; margin:0 0 8px 0;\" class=\"dark-msg\">We received a login attempt for <strong style=\"color:#111827;\" class=\"dark-text\">%s</strong>. Enter the code above to verify your identity.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#fffbeb; border-radius:8px; margin:20px 0 28px 0;\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:16px 18px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"font-size:20px; vertical-align:top; padding-right:12px;\"></td>\n" +
                        "                      <td style=\"font-size:14px; color:#78350f; line-height:1.5;\" class=\"dark-warn-text\"><strong style=\"color:#451a03;\" class=\"dark-warn-strong\">Didn't attempt to log in?</strong> &nbsp;Please ignore this email. Your account remains secure.</td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "              <tr>\n" +
                        "                <td align=\"center\">\n" +
                        "                  <a href=\"#\" style=\"display:block; background-color:#4f46e5; color:#ffffff !important; font-weight:600; font-size:15px; padding:13px 20px; border-radius:12px; text-decoration:none; text-align:center;\" class=\"dark-btn\">Verify Login</a>\n" +
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
                        "</html>";

        template = template.replaceAll("%(?![s])", "%%");
        return String.format(template, otpCode, expiryText, recipientEmail);
    }

    public String buildDeleteAccountEmailBody(String otpCode, String recipientEmail) {
        String expiryText = "5 minutes";
        String template =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "  <title>Delete Account</title>\n" +
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
                        "      .dark-btn   { background-color: #ef4444 !important; }\n" +
                        "      .dark-foot  { color: #6b7280 !important; }\n" +
                        "      .dark-msg   { color: #d1d5db !important; }\n" +
                        "      .dark-warn-text { color: #fca5a5 !important; }\n" +
                        "      .dark-warn-strong { color: #fecaca !important; }\n" +
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
                        "                      <td style=\"width:40px; height:40px; background:linear-gradient(135deg,#ef4444,#dc2626); border-radius:10px; text-align:center; vertical-align:middle; color:#fff; font-weight:700; font-size:18px; letter-spacing:-0.3px;\">SP</td>\n" +
                        "                      <td style=\"padding-left:10px; font-size:20px; font-weight:700; color:#111827; letter-spacing:-0.3px;\" class=\"dark-text\">Secure<span style=\"color:#4f46e5;\">Pass</span></td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <h1 style=\"font-size:26px; font-weight:700; color:#111827; margin:0 0 6px 0; letter-spacing:-0.3px;\" class=\"dark-text\">Delete account</h1>\n" +
                        "            <p style=\"font-size:15px; color:#6b7280; margin:0 0 28px 0; line-height:1.5;\" class=\"dark-sub\">Enter the code below to permanently delete your account.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f8fafc; border-radius:14px; border:2px dashed #e2e8f0; margin-bottom:28px;\" class=\"dark-otp\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:28px 16px; text-align:center;\">\n" +
                        "                  <div style=\"font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:1.2px; color:#6b7280; margin-bottom:10px;\" class=\"dark-muted\">verification code</div>\n" +
                        "                  <div style=\"font-size:48px; font-weight:700; letter-spacing:10px; color:#111827; font-family:'SF Mono', 'Menlo', 'Consolas', monospace;\" class=\"dark-code\">%s</div>\n" +
                        "                  <div style=\"font-size:13px; color:#9ca3af; margin-top:12px;\" class=\"dark-hint\">expires in <span style=\"font-weight:600; color:#4f46e5;\">%s</span></div>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <p style=\"font-size:15px; color:#374151; line-height:1.7; margin:0 0 8px 0;\" class=\"dark-msg\">We received a request to delete the account associated with <strong style=\"color:#111827;\" class=\"dark-text\">%s</strong>. This action is irreversible.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#fef2f2; border-radius:8px; margin:20px 0 28px 0;\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:16px 18px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"font-size:20px; vertical-align:top; padding-right:12px;\"></td>\n" +
                        "                      <td style=\"font-size:14px; color:#991b1b; line-height:1.5;\" class=\"dark-warn-text\"><strong style=\"color:#7f1d1d;\" class=\"dark-warn-strong\">Didn't request this?</strong> &nbsp;Please ignore this email. Your account will remain active.</td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "              <tr>\n" +
                        "                <td align=\"center\">\n" +
                        "                  <a href=\"#\" style=\"display:block; background-color:#ef4444; color:#ffffff !important; font-weight:600; font-size:15px; padding:13px 20px; border-radius:12px; text-decoration:none; text-align:center;\" class=\"dark-btn\">Confirm Delete</a>\n" +
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
                        "</html>";

        template = template.replaceAll("%(?![s])", "%%");
        return String.format(template, otpCode, expiryText, recipientEmail);
    }

    public String buildGenericOtpEmail(String otpCode, String recipientEmail) {
        String expiryText = "5 minutes";
        String template =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "  <title>Your OTP Code</title>\n" +
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
                        "            <h1 style=\"font-size:26px; font-weight:700; color:#111827; margin:0 0 6px 0; letter-spacing:-0.3px;\" class=\"dark-text\">Verification Code</h1>\n" +
                        "            <p style=\"font-size:15px; color:#6b7280; margin:0 0 28px 0; line-height:1.5;\" class=\"dark-sub\">Enter the code below to complete your action.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f8fafc; border-radius:14px; border:2px dashed #e2e8f0; margin-bottom:28px;\" class=\"dark-otp\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:28px 16px; text-align:center;\">\n" +
                        "                  <div style=\"font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:1.2px; color:#6b7280; margin-bottom:10px;\" class=\"dark-muted\">verification code</div>\n" +
                        "                  <div style=\"font-size:48px; font-weight:700; letter-spacing:10px; color:#111827; font-family:'SF Mono', 'Menlo', 'Consolas', monospace;\" class=\"dark-code\">%s</div>\n" +
                        "                  <div style=\"font-size:13px; color:#9ca3af; margin-top:12px;\" class=\"dark-hint\">expires in <span style=\"font-weight:600; color:#4f46e5;\">%s</span></div>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <p style=\"font-size:15px; color:#374151; line-height:1.7; margin:0 0 8px 0;\" class=\"dark-msg\">We received a request that requires verification for <strong style=\"color:#111827;\" class=\"dark-text\">%s</strong>. Enter the code above to proceed.</p>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#fffbeb; border-radius:8px; margin:20px 0 28px 0;\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"padding:16px 18px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"font-size:20px; vertical-align:top; padding-right:12px;\"></td>\n" +
                        "                      <td style=\"font-size:14px; color:#78350f; line-height:1.5;\" class=\"dark-warn-text\"><strong style=\"color:#451a03;\" class=\"dark-warn-strong\">Didn't request this?</strong> &nbsp;Please ignore this email. No action will be taken.</td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "\n" +
                        "            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                        "              <tr>\n" +
                        "                <td align=\"center\">\n" +
                        "                  <a href=\"#\" style=\"display:block; background-color:#4f46e5; color:#ffffff !important; font-weight:600; font-size:15px; padding:13px 20px; border-radius:12px; text-decoration:none; text-align:center;\" class=\"dark-btn\">Verify</a>\n" +
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
                        "</html>";

        template = template.replaceAll("%(?![s])", "%%");
        return String.format(template, otpCode, expiryText, recipientEmail);
    }
}
