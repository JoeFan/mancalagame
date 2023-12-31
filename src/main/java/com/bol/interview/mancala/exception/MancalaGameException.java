package com.bol.interview.mancala.exception;

public class MancalaGameException extends RuntimeException {
    public MancalaGameException(String msg) {
        super(msg);
    }

    public MancalaGameException(String message, Throwable e) {
        super(message, e);
    }
}
