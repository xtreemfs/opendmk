package com.sun.jdmk.security.acl;

import java.security.Principal;

/**
 * This interface replaces the deprecated java.security.acl interfaces.
 */

public interface Owner {

    boolean addOwner​(Principal caller, Principal owner)
	throws NotOwnerException;
}
