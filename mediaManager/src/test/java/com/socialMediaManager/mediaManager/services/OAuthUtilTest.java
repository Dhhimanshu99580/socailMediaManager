package com.socialMediaManager.mediaManager.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OAuthUtilTest {

    @Test
    void generateCodeVerifier_returnsNonNullValue() {
        String verifier = OAuthUtil.generateCodeVerifier();
        assertNotNull(verifier);
        assertFalse(verifier.isBlank());
    }

    @Test
    void generateCodeVerifier_returnsDifferentValuesOnEachCall() {
        String v1 = OAuthUtil.generateCodeVerifier();
        String v2 = OAuthUtil.generateCodeVerifier();
        assertNotEquals(v1, v2);
    }

    @Test
    void generateCodeVerifier_isBase64UrlEncoded() {
        String verifier = OAuthUtil.generateCodeVerifier();
        // base64url uses only A-Z a-z 0-9 - _ (no padding)
        assertTrue(verifier.matches("[A-Za-z0-9\\-_]+"));
    }

    @Test
    void generateCodeChallenge_returnsNonNullValue() throws Exception {
        String challenge = OAuthUtil.generateCodeChallenge("someVerifier");
        assertNotNull(challenge);
        assertFalse(challenge.isBlank());
    }

    @Test
    void generateCodeChallenge_isDifferentFromVerifier() throws Exception {
        String verifier = "someCodeVerifier";
        String challenge = OAuthUtil.generateCodeChallenge(verifier);
        assertNotEquals(verifier, challenge);
    }

    @Test
    void generateCodeChallenge_sameInputProducesSameOutput() throws Exception {
        String verifier = OAuthUtil.generateCodeVerifier();
        String challenge1 = OAuthUtil.generateCodeChallenge(verifier);
        String challenge2 = OAuthUtil.generateCodeChallenge(verifier);
        assertEquals(challenge1, challenge2);
    }

    @Test
    void generateCodeChallenge_isBase64UrlEncoded() throws Exception {
        String challenge = OAuthUtil.generateCodeChallenge("testVerifier");
        assertTrue(challenge.matches("[A-Za-z0-9\\-_]+"));
    }
}
