package com.socialMediaManager.mediaManager.services;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.MessageDigest;
public class OAuthUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    public static String generateCodeVerifier(){
        byte[] randombytes = new byte[32];
        secureRandom.nextBytes(randombytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randombytes);
    }
    public static String generateCodeChallenge(String codeverifier) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(codeverifier.getBytes("UTF-8"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
