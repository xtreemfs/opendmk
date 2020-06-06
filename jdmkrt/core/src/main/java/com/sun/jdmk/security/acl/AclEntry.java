package com.sun.jdmk.security.acl;

import java.util.Enumeration;
import java.security.Principal;

/**
 * This interface replaces the deprecated java.security.acl interfaces.
 */

public interface AclEntry extends Cloneable {

    boolean setPrincipal(Principal user);

    Principal getPrincipal();

    void setNegativePermissions();

    boolean isNegative();

    boolean addPermission(Permission permission);

    boolean removePermission(Permission permission);

    boolean checkPermission(Permission permission);

    Enumeration<Permission> permissions();

    String toString();

    Object clone();
}
