package com.mxmind.tripleware.rxflow.exceptions;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class RxTransitionException extends RuntimeException {

    public RxTransitionException(Cause cause) {
        super(cause.message);
    }

    public RxTransitionException(Throwable ex, Cause cause) {
        super(cause.message, ex);
    }

    public enum Cause {

        BEFORE, AFTER;

        private final String message;

        private static final String MESSAGE_PTR = "Fails to execute %s";

        Cause() {
            this.message = String.format(MESSAGE_PTR, this.name().toLowerCase());
        }
    }
}