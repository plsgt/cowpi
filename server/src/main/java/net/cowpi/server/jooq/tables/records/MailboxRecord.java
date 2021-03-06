/*
 * This file is generated by jOOQ.
 */
package net.cowpi.server.jooq.tables.records;


import javax.annotation.Generated;

import net.cowpi.server.jooq.tables.Mailbox;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MailboxRecord extends UpdatableRecordImpl<MailboxRecord> implements Record3<Long, Long, byte[]> {

    private static final long serialVersionUID = 1274500773;

    /**
     * Setter for <code>router.mailbox.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>router.mailbox.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>router.mailbox.user_to</code>.
     */
    public void setUserTo(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>router.mailbox.user_to</code>.
     */
    public Long getUserTo() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>router.mailbox.message</code>.
     */
    public void setMessage(byte... value) {
        set(2, value);
    }

    /**
     * Getter for <code>router.mailbox.message</code>.
     */
    public byte[] getMessage() {
        return (byte[]) get(2);
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
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, Long, byte[]> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, Long, byte[]> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Mailbox.MAILBOX.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return Mailbox.MAILBOX.USER_TO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<byte[]> field3() {
        return Mailbox.MAILBOX.MESSAGE;
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
        return getUserTo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] component3() {
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
        return getUserTo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] value3() {
        return getMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailboxRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailboxRecord value2(Long value) {
        setUserTo(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailboxRecord value3(byte... value) {
        setMessage(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailboxRecord values(Long value1, Long value2, byte[] value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MailboxRecord
     */
    public MailboxRecord() {
        super(Mailbox.MAILBOX);
    }

    /**
     * Create a detached, initialised MailboxRecord
     */
    public MailboxRecord(Long id, Long userTo, byte[] message) {
        super(Mailbox.MAILBOX);

        set(0, id);
        set(1, userTo);
        set(2, message);
    }
}
