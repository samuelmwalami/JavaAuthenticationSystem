package modules.authentication.DTO.commonDTO;

import lombok.Getter;

public class EventOtpDTO {
    @Getter
    String mailTo;
    @Getter
    String otp;
    
    public EventOtpDTO(String mailTo, String otp){
        this.mailTo = mailTo;
        this.otp = otp;
    }
}
