package modules.authentication.Domain;

public class User {

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

}
