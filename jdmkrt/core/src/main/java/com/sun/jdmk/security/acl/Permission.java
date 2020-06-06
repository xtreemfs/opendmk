package com.sun.jdmk.security.acl;

/**
 * This interface replaces the deprecated java.security.acl interfaces.
 */

public interface Permission {

    boolean equals(Object another);

    String toString();
}
