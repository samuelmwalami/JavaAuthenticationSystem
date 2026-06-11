package modules.authentication.repository.security;

public interface MessageDigestRepository {
    public String hashPassword(String plainPassword);
    public boolean verifyPassword(String storedHash, String plainPassword);
}
