package org.pdfquill;

/**
 * Runtime exception that signals unrecoverable PDF creation issues.
 */
public class PrinterException extends RuntimeException {
    private static final long serialVersionUID = -2485028966274019623L;

    /**
     * Creates an exception with the supplied message and underlying cause.
     *
     * @param message human-readable message
     * @param cause   wrapped exception
     */
    public PrinterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception with the supplied message.
     *
     * @param message human-readable message
     */
    public PrinterException(String message) {
        super(message);
    }
}
