package com.socialMediaManager.mediaManager.exceptions;

public class userAlreadyExistsException extends RuntimeException {
    public userAlreadyExistsException (String msg) {
        super(msg);
    }
}
