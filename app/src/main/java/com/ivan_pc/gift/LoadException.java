package com.ivan_pc.gift;

/**
 * Created by Ivan-PC on 02.01.2017.
 */

public class LoadException extends Exception {
    private final String reason;

    LoadException(String reason) {
        super();
        this.reason = reason;
    }

    String getReason() {
        return reason;
    }
}
