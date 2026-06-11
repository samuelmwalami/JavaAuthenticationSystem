package modules.authentication.DTO.responseDTO;

import lombok.Getter;

@Getter
public class ApiResponse {
    int statusCode;
    ResponseBody content;

    public ApiResponse(){}
    public ApiResponse(int statusCode, ResponseBody content){
       this.statusCode = statusCode;
       this.content = content;
    }
}
