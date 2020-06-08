package com.sun.jdmk.security.acl;

/**
 * A replacement for the deprecated java.security.acl.NotOwnerException
 */

public class NotOwnerException extends Exception {

    private static final long serialVersionUID = 3382143188777171891L;

    public NotOwnerException() {}
}
