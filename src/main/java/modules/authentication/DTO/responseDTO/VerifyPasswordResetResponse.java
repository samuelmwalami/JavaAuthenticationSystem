package modules.authentication.DTO.responseDTO;

public class VerifyPasswordResetResponse extends ResponseBody{
    String message;

    public VerifyPasswordResetResponse(){}
    public VerifyPasswordResetResponse(String message){
        this.message = message;
    }
}
