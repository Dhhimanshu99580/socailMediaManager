package com.socialMediaManager.mediaManager.Exceptions;

public class userAlreadyExistsException extends RuntimeException {
    public userAlreadyExistsException (String msg) {
        super(msg);
    }
}
