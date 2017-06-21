package com.tingtingfm.cbb.ui.play;

/**
 * @author jpeg
 *
 */
public class LibMediaException extends Exception {
    private static final long serialVersionUID = -1909522348226924189L;

    /**
     * Create an empty error
     */
    public LibMediaException() {
        super();
    }

    /**
     * @param detailMessage
     */
    public LibMediaException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * @param throwable
     */
    public LibMediaException(Throwable throwable) {
        super(throwable);
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public LibMediaException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
