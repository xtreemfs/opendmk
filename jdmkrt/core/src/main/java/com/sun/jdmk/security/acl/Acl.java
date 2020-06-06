package com.sun.jdmk.security.acl;

import java.util.Enumeration;
import java.security.Principal;

/**
 * This interface replaces the deprecated java.security.acl interfaces.
 */

public interface Acl extends Owner {

    void setName(Principal caller, String name) throws NotOwnerException;

    String getName();

    boolean addEntry(Principal caller, AclEntry entry) throws NotOwnerException;

    boolean removeEntry(Principal caller, AclEntry entry) throws NotOwnerException;

    Enumeration<Permission> getPermissions(Principal user);

    Enumeration<AclEntry> entries();

    boolean checkPermission(Principal principal, Permission permission);

    String toString();
}
