package modules.authentication.repository.storage;

import modules.authentication.DTO.commonDTO.OtpDTO;

public interface OtpRepository {
    public int saveOtp(OtpDTO otpDTO);
    public OtpDTO retrieveOtpByOtpAndEmail(String otp, String email);
    public OtpDTO retrieveOtpByEmail(String email);
    public int deleteOtpByEmail(String email);
}
