package com.socialMediaManager.mediaManager.exceptions;

public class userDoesNotExistException extends RuntimeException{
    public userDoesNotExistException(String msg) {
        super(msg);
    }
}
