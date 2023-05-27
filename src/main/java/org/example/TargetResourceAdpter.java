package org.example;

import java.io.ByteArrayInputStream;

/**
 *
 * @author igor.ramos - Jun 18, 2018
 */
public interface TargetResourceAdpter<T extends Object> extends AutoCloseable {

    public TargetResourceAdpter<T> print(String text) throws PrinterException;

    public TargetResourceAdpter<T> print(String text, int param) throws PrinterException;

    public T getResource();

    public default TargetResourceAdpter<T> printImage(ByteArrayInputStream imgBytes) {
        return null;
    }

    public default TargetResourceAdpter<T> printBarcode(String code, Integer... params) throws PrinterException {
        return null;
    }

    public default TargetResourceAdpter<T> cutSignal() throws PrinterException {
        return null;
    }

    public default Long status() throws PrinterException {
        return 2l;
    }

    public default Integer getMaxLineLength() {
        return 56;
    }

    public default Integer getMaxLineLength(int param) {
        return this.getMaxLineLength();
    }
}

