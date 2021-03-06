/*
 * This file is generated by jOOQ.
 */
package net.cowpi.oes.jooq;


import javax.annotation.Generated;

import org.jooq.Sequence;
import org.jooq.impl.SequenceImpl;


/**
 * Convenience access to all sequences in 
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sequences {

    /**
     * The sequence <code>conversation_id_seq</code>
     */
    public static final Sequence<Long> CONVERSATION_ID_SEQ = new SequenceImpl<Long>("conversation_id_seq", DefaultSchema.DEFAULT_SCHEMA, org.jooq.impl.SQLDataType.BIGINT.nullable(false));

    /**
     * The sequence <code>local_key_id_seq</code>
     */
    public static final Sequence<Long> LOCAL_KEY_ID_SEQ = new SequenceImpl<Long>("local_key_id_seq", DefaultSchema.DEFAULT_SCHEMA, org.jooq.impl.SQLDataType.BIGINT.nullable(false));

    /**
     * The sequence <code>participant_state_id_seq</code>
     */
    public static final Sequence<Long> PARTICIPANT_STATE_ID_SEQ = new SequenceImpl<Long>("participant_state_id_seq", DefaultSchema.DEFAULT_SCHEMA, org.jooq.impl.SQLDataType.BIGINT.nullable(false));

    /**
     * The sequence <code>prekey_id_seq</code>
     */
    public static final Sequence<Long> PREKEY_ID_SEQ = new SequenceImpl<Long>("prekey_id_seq", DefaultSchema.DEFAULT_SCHEMA, org.jooq.impl.SQLDataType.BIGINT.nullable(false));

    /**
     * The sequence <code>router_mailbox_id_seq</code>
     */
    public static final Sequence<Long> ROUTER_MAILBOX_ID_SEQ = new SequenceImpl<Long>("router_mailbox_id_seq", DefaultSchema.DEFAULT_SCHEMA, org.jooq.impl.SQLDataType.BIGINT.nullable(false));
}
