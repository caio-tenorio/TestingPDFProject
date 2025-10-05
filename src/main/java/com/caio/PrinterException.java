package com.caio;

public class PrinterException extends RuntimeException {
    private static final long serialVersionUID = -2485028966274019623L;

    public PrinterException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrinterException(String message) {
        super(message);
    }
}
