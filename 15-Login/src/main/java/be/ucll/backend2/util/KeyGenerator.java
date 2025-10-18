package be.ucll.backend2.util;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        final var base64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(key);
        System.out.println(base64Key);
    }
}
