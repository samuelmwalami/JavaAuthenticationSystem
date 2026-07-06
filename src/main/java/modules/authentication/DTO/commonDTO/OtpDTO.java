package modules.authentication.DTO.commonDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class OtpDTO {
    UUID otpID;
    String otp;
    LocalDateTime otpExpiry;
    String userEmail;

    public OtpDTO(){
    }
    public OtpDTO(UUID otpID, String otp, LocalDateTime otpExpiry, String userEmail){
        this.otpID = otpID;
        this.otp = otp;
        this.otpExpiry = otpExpiry;
        this.userEmail = userEmail;
    }
}
