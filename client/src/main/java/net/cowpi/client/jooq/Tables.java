/*
 * This file is generated by jOOQ.
 */
package net.cowpi.client.jooq;


import javax.annotation.Generated;

import net.cowpi.client.jooq.tables.CowpiUser;
import net.cowpi.client.jooq.tables.Prekey;


/**
 * Convenience access to all tables in 
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>cowpi_user</code>.
     */
    public static final CowpiUser COWPI_USER = net.cowpi.client.jooq.tables.CowpiUser.COWPI_USER;

    /**
     * The table <code>prekey</code>.
     */
    public static final Prekey PREKEY = net.cowpi.client.jooq.tables.Prekey.PREKEY;
}