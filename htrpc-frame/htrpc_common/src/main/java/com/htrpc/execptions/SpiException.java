package com.htrpc.execptions;

public class SpiException extends RuntimeException{
    public SpiException() {
    }

    public SpiException(String message) {
        super(message);
    }

    public SpiException(Throwable cause) {
        super(cause);
    }
}
