package com.socialMediaManager.mediaManager.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OAuthStateStoreTest {

    private OAuthStateStore stateStore;

    @BeforeEach
    void setUp() {
        stateStore = new OAuthStateStore();
    }

    @Test
    void save_thenIsValid_returnsTrue() {
        stateStore.save("state123", "verifier", "user1");
        assertTrue(stateStore.isValid("state123"));
    }

    @Test
    void isValid_unknownState_returnsFalse() {
        assertFalse(stateStore.isValid("nonexistent"));
    }

    @Test
    void getCodeVerifier_returnsCorrectValue() {
        stateStore.save("state123", "myVerifier", "user1");
        assertEquals("myVerifier", stateStore.getCodeVerifier("state123"));
    }

    @Test
    void getUsername_returnsCorrectValue() {
        stateStore.save("state123", "verifier", "himanshu");
        assertEquals("himanshu", stateStore.getUsername("state123"));
    }

    @Test
    void remove_stateNoLongerValid() {
        stateStore.save("state123", "verifier", "user1");
        stateStore.remove("state123");
        assertFalse(stateStore.isValid("state123"));
    }

    @Test
    void getCodeVerifier_unknownState_returnsNull() {
        assertNull(stateStore.getCodeVerifier("nonexistent"));
    }

    @Test
    void getUsername_unknownState_returnsNull() {
        assertNull(stateStore.getUsername("nonexistent"));
    }

    @Test
    void save_multipleStates_eachIndependent() {
        stateStore.save("stateA", "verifierA", "userA");
        stateStore.save("stateB", "verifierB", "userB");

        assertEquals("verifierA", stateStore.getCodeVerifier("stateA"));
        assertEquals("verifierB", stateStore.getCodeVerifier("stateB"));
        assertEquals("userA", stateStore.getUsername("stateA"));
        assertEquals("userB", stateStore.getUsername("stateB"));
    }
}
