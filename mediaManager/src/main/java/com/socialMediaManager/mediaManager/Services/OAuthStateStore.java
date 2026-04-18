package com.socialMediaManager.mediaManager.services;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OAuthStateStore {

    private record StateEntry(String codeVerifier, String username) {}

    private final ConcurrentHashMap<String, StateEntry> store = new ConcurrentHashMap<>();

    public void save(String state, String codeVerifier, String username) {
        store.put(state, new StateEntry(codeVerifier, username));
    }

    public String getCodeVerifier(String state) {
        StateEntry entry = store.get(state);
        return entry != null ? entry.codeVerifier() : null;
    }

    public String getUsername(String state) {
        StateEntry entry = store.get(state);
        return entry != null ? entry.username() : null;
    }

    public boolean isValid(String state) {
        return store.containsKey(state);
    }

    public void remove(String state) {
        store.remove(state);
    }
}
