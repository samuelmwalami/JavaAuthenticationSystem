package modules.authentication.Domain;


import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
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
    LocalDateTime createdAt;

    public User(){}
    public User(UUID userId,
                String firstName,
                String lastName,
                String userName,
                String email,
                String password,
                LocalDateTime createdAt){
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }
    


    // password logic
    public static boolean isPasswordStrong(String password){
        if(password == null){
            return false;
        }
        Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
    public static boolean doPasswordsMatch(String password, String confirmPassword){
        if(password == null || confirmPassword == null){
            return false;
        }
        return password.strip().equals(confirmPassword.strip());
    }


    // name logic
    private boolean isNameValid(String name){
        if(name == null){
            return false;
        }
        return name.matches("^[a-z0-9_]{3,20}$") ;
    }

    public boolean isFirstNameValid(){
        return isNameValid(this.firstName);
    }

    public boolean isLastNameValid(){
        return isNameValid(this.lastName);
    }

    public boolean isUserNameValid(){ return isNameValid(this.userName); }



    // email logic
    public boolean isEmailValid(){
        if(this.email == null){
            return false;
        }
        Pattern pattern = Pattern.compile("^(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}|(?:\\[(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\]))$");
        Matcher matcher = pattern.matcher(this.email);
        return matcher.find();
    }



    public UserDTO userToUserDTOMapper(){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(this.userId);
        userDTO.setFirstName(this.firstName.toLowerCase());
        userDTO.setLastName(this.lastName.toLowerCase());
        userDTO.setUserName(this.userName.toLowerCase());
        userDTO.setEmail(this.email.toLowerCase());
        userDTO.setPassword(this.password);
        return userDTO;
    }
}
