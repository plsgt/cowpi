/*
 * This file is generated by jOOQ.
 */
package net.cowpi.server.jooq.tables.records;


import javax.annotation.Generated;

import net.cowpi.server.jooq.tables.OesPrekey;

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
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OesPrekeyRecord extends UpdatableRecordImpl<OesPrekeyRecord> implements Record4<Long, Long, Long, byte[]> {

    private static final long serialVersionUID = -1329539661;

    /**
     * Setter for <code>router.oes_prekey.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>router.oes_prekey.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>router.oes_prekey.oes_id</code>.
     */
    public void setOesId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>router.oes_prekey.oes_id</code>.
     */
    public Long getOesId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>router.oes_prekey.key_id</code>.
     */
    public void setKeyId(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>router.oes_prekey.key_id</code>.
     */
    public Long getKeyId() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>router.oes_prekey.prekey</code>.
     */
    public void setPrekey(byte... value) {
        set(3, value);
    }

    /**
     * Getter for <code>router.oes_prekey.prekey</code>.
     */
    public byte[] getPrekey() {
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
        return OesPrekey.OES_PREKEY.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return OesPrekey.OES_PREKEY.OES_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return OesPrekey.OES_PREKEY.KEY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<byte[]> field4() {
        return OesPrekey.OES_PREKEY.PREKEY;
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
        return getOesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component3() {
        return getKeyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] component4() {
        return getPrekey();
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
        return getOesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getKeyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] value4() {
        return getPrekey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OesPrekeyRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OesPrekeyRecord value2(Long value) {
        setOesId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OesPrekeyRecord value3(Long value) {
        setKeyId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OesPrekeyRecord value4(byte... value) {
        setPrekey(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OesPrekeyRecord values(Long value1, Long value2, Long value3, byte[] value4) {
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
     * Create a detached OesPrekeyRecord
     */
    public OesPrekeyRecord() {
        super(OesPrekey.OES_PREKEY);
    }

    /**
     * Create a detached, initialised OesPrekeyRecord
     */
    public OesPrekeyRecord(Long id, Long oesId, Long keyId, byte[] prekey) {
        super(OesPrekey.OES_PREKEY);

        set(0, id);
        set(1, oesId);
        set(2, keyId);
        set(3, prekey);
    }
}
