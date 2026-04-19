package com.socialMediaManager.mediaManager.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "socialMediaManagerSecretKey123456");
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", 3600000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtTokenProvider.generateToken("testuser");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");
        assertNotEquals(token1, token2);
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateToken("testuser");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtTokenProvider.generateToken("testuser");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", -1000L);
        String token = jwtTokenProvider.generateToken("testuser");
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_randomString_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("not.a.jwt.token"));
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtTokenProvider.generateToken("himanshu");
        assertEquals("himanshu", jwtTokenProvider.getUsernameFromToken(token));
    }
}
