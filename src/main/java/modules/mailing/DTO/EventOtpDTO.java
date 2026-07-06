package modules.mailing.DTO;

import lombok.Getter;

public class EventOtpDTO {
    @Getter
    String mailTo;
    @Getter
    String otp;
}
