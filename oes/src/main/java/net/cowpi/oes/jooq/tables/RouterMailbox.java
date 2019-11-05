/*
 * This file is generated by jOOQ.
 */
package net.cowpi.oes.jooq.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import net.cowpi.oes.jooq.DefaultSchema;
import net.cowpi.oes.jooq.Indexes;
import net.cowpi.oes.jooq.Keys;
import net.cowpi.oes.jooq.tables.records.RouterMailboxRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


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
public class RouterMailbox extends TableImpl<RouterMailboxRecord> {

    private static final long serialVersionUID = 709817013;

    /**
     * The reference instance of <code>router_mailbox</code>
     */
    public static final RouterMailbox ROUTER_MAILBOX = new RouterMailbox();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RouterMailboxRecord> getRecordType() {
        return RouterMailboxRecord.class;
    }

    /**
     * The column <code>router_mailbox.id</code>.
     */
    public final TableField<RouterMailboxRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('oes_1.router_mailbox_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>router_mailbox.conversation</code>.
     */
    public final TableField<RouterMailboxRecord, Long> CONVERSATION = createField("conversation", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>router_mailbox.message_index</code>.
     */
    public final TableField<RouterMailboxRecord, Long> MESSAGE_INDEX = createField("message_index", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>router_mailbox.message</code>.
     */
    public final TableField<RouterMailboxRecord, byte[]> MESSAGE = createField("message", org.jooq.impl.SQLDataType.BLOB.nullable(false), this, "");

    /**
     * Create a <code>router_mailbox</code> table reference
     */
    public RouterMailbox() {
        this(DSL.name("router_mailbox"), null);
    }

    /**
     * Create an aliased <code>router_mailbox</code> table reference
     */
    public RouterMailbox(String alias) {
        this(DSL.name(alias), ROUTER_MAILBOX);
    }

    /**
     * Create an aliased <code>router_mailbox</code> table reference
     */
    public RouterMailbox(Name alias) {
        this(alias, ROUTER_MAILBOX);
    }

    private RouterMailbox(Name alias, Table<RouterMailboxRecord> aliased) {
        this(alias, aliased, null);
    }

    private RouterMailbox(Name alias, Table<RouterMailboxRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> RouterMailbox(Table<O> child, ForeignKey<O, RouterMailboxRecord> key) {
        super(child, key, ROUTER_MAILBOX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.ROUTER_MAILBOX_CONVERSATION_MESSAGE_INDEX_KEY, Indexes.ROUTER_MAILBOX_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<RouterMailboxRecord, Long> getIdentity() {
        return Keys.IDENTITY_ROUTER_MAILBOX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RouterMailboxRecord> getPrimaryKey() {
        return Keys.ROUTER_MAILBOX_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RouterMailboxRecord>> getKeys() {
        return Arrays.<UniqueKey<RouterMailboxRecord>>asList(Keys.ROUTER_MAILBOX_PKEY, Keys.ROUTER_MAILBOX_CONVERSATION_MESSAGE_INDEX_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<RouterMailboxRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<RouterMailboxRecord, ?>>asList(Keys.ROUTER_MAILBOX__ROUTER_MAILBOX_CONVERSATION_FKEY);
    }

    public Conversation conversation() {
        return new Conversation(this, Keys.ROUTER_MAILBOX__ROUTER_MAILBOX_CONVERSATION_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouterMailbox as(String alias) {
        return new RouterMailbox(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouterMailbox as(Name alias) {
        return new RouterMailbox(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RouterMailbox rename(String name) {
        return new RouterMailbox(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RouterMailbox rename(Name name) {
        return new RouterMailbox(name, null);
    }
}