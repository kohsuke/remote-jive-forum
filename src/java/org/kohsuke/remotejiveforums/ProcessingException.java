package org.kohsuke.remotejiveforums;

import java.rmi.RemoteException;

/**
 * Represents an error during the processing.
 *
 * @author Kohsuke Kawaguchi
 */
public class ProcessingException extends RemoteException {
    public ProcessingException() {
        super();
    }

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessingException(Throwable cause) {
        super();
        detail = cause;
    }
}
