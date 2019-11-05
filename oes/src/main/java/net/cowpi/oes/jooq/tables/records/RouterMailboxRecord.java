/*
 * This file is generated by jOOQ.
 */
package net.cowpi.oes.jooq.tables.records;


import javax.annotation.Generated;

import net.cowpi.oes.jooq.tables.RouterMailbox;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RouterMailboxRecord extends UpdatableRecordImpl<RouterMailboxRecord> implements Record4<Long, Long, Long, byte[]> {

    private static final long serialVersionUID = -1085651822;

    /**
     * Setter for <code>router_mailbox.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>router_mailbox.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>router_mailbox.conversation</code>.
     */
    public void setConversation(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>router_mailbox.conversation</code>.
     */
    public Long getConversation() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>router_mailbox.message_index</code>.
     */
    public void setMessageIndex(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>router_mailbox.message_index</code>.
     */
    public Long getMessageIndex() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>router_mailbox.message</code>.
     */
    public void setMessage(byte... value) {
        set(3, value);
    }

    /**
     * Getter for <code>router_mailbox.message</code>.
     */
    public byte[] getMessage() {
        return (byte[]) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Long, Long, Long, byte[]> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Long, Long, Long, byte[]> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return RouterMailbox.ROUTER_MAILBOX.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return RouterMailbox.ROUTER_MAILBOX.CONVERSATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return RouterMailbox.ROUTER_MAILBOX.MESSAGE_INDEX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<byte[]> field4() {
        return RouterMailbox.ROUTER_MAILBOX.MESSAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component2() {
        return getConversation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component3() {
        return getMessageIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] component4() {
        return getMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getConversation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getMessageIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] value4() {
        return getMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouterMailboxRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouterMailboxRecord value2(Long value) {
        setConversation(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouterMailboxRecord value3(Long value) {
        setMessageIndex(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouterMailboxRecord value4(byte... value) {
        setMessage(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouterMailboxRecord values(Long value1, Long value2, Long value3, byte[] value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RouterMailboxRecord
     */
    public RouterMailboxRecord() {
        super(RouterMailbox.ROUTER_MAILBOX);
    }

    /**
     * Create a detached, initialised RouterMailboxRecord
     */
    public RouterMailboxRecord(Long id, Long conversation, Long messageIndex, byte[] message) {
        super(RouterMailbox.ROUTER_MAILBOX);

        set(0, id);
        set(1, conversation);
        set(2, messageIndex);
        set(3, message);
    }
}