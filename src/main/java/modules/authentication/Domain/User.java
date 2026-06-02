package modules.authentication.Domain;


import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import modules.authentication.DTO.commonDTO.UserDTO;

@Getter
@Setter
public class User {
    UUID userId;
    String firstName;
    String lastName;
    String userName;
    String email;
    String password;
    String confirmPassword;
    LocalDateTime createdAt;

    // password logic
    public boolean isPasswordStrong(String password){
        return password.strip().length() < 8;
    }
    public boolean doPasswordsMatch(String password, String confirmPassword){
        return password.strip().equals(confirmPassword.strip());
    }


    // name logic
    public boolean isNameValid(String name){
        return name != null && !name.isEmpty() && name.length()<50 ;
    }


    // email logic
    public boolean isEmailValid(String email){
        return email.contains("@") && email.contains(".");
    }

    public UserDTO userToUserDTOMapper(){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(this.userId);
        userDTO.setFirstName(this.firstName);
        userDTO.setLastName(this.lastName);
        userDTO.setEmail(this.email);
        userDTO.setPassword(this.password);
        userDTO.setConfirmPassword(this.confirmPassword);
        return userDTO;
    }
}
