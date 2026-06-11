package modules.authentication.infrastructure.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import modules.authentication.repository.security.MessageDigestRepository;

public class MessageDigest implements MessageDigestRepository {
    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public String hashPassword(String plainPassword){
        return ARGON2.hash(3,65536,1, plainPassword.toCharArray());
    }

    public boolean verifyPassword(String storedHash, String plainPassword){
        return ARGON2.verify(storedHash, plainPassword);
    }
}
