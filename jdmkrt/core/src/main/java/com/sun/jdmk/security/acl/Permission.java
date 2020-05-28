package com.sun.jdmk.security.acl;

public interface Permission {

    /**
     * Returns true if the object passed matches the permission represented
     * in this interface.
     *
     * @param another the Permission object to compare with.
     *
     * @return true if the Permission objects are equal, false otherwise
     */
    public boolean equals(Object another);

    /**
     * Prints a string representation of this permission.
     *
     * @return the string representation of the permission.
     */
    public String toString();

}
