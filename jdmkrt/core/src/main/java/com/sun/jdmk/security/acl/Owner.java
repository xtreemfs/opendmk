package com.sun.jdmk.security.acl;

import java.security.Principal;

public interface Owner {

    boolean addOwner​(Principal caller, Principal owner)
	throws NotOwnerException;
}
