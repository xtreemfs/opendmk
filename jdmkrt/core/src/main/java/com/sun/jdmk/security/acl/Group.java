package com.sun.jdmk.security.acl;

import java.util.Enumeration;
import java.security.Principal;

/**
 * This interface replaces the deprecated java.security.acl interfaces.
 */

public interface Group extends Principal {

    boolean addMember(Principal user);

    boolean removeMember(Principal user);

    boolean isMember(Principal member);

    Enumeration<? extends Principal> members();
}
