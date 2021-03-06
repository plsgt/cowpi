/*
 * This file is generated by jOOQ.
 */
package net.cowpi.server.jooq.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import net.cowpi.server.jooq.Indexes;
import net.cowpi.server.jooq.Keys;
import net.cowpi.server.jooq.Router;
import net.cowpi.server.jooq.tables.records.PrekeyRecord;

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
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Prekey extends TableImpl<PrekeyRecord> {

    private static final long serialVersionUID = -1906296222;

    /**
     * The reference instance of <code>router.prekey</code>
     */
    public static final Prekey PREKEY = new Prekey();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PrekeyRecord> getRecordType() {
        return PrekeyRecord.class;
    }

    /**
     * The column <code>router.prekey.id</code>.
     */
    public final TableField<PrekeyRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('router.prekey_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>router.prekey.user_id</code>.
     */
    public final TableField<PrekeyRecord, Long> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>router.prekey.key_id</code>.
     */
    public final TableField<PrekeyRecord, Long> KEY_ID = createField("key_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>router.prekey.prekey</code>.
     */
    public final TableField<PrekeyRecord, byte[]> PREKEY_ = createField("prekey", org.jooq.impl.SQLDataType.BLOB.nullable(false), this, "");

    /**
     * Create a <code>router.prekey</code> table reference
     */
    public Prekey() {
        this(DSL.name("prekey"), null);
    }

    /**
     * Create an aliased <code>router.prekey</code> table reference
     */
    public Prekey(String alias) {
        this(DSL.name(alias), PREKEY);
    }

    /**
     * Create an aliased <code>router.prekey</code> table reference
     */
    public Prekey(Name alias) {
        this(alias, PREKEY);
    }

    private Prekey(Name alias, Table<PrekeyRecord> aliased) {
        this(alias, aliased, null);
    }

    private Prekey(Name alias, Table<PrekeyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Prekey(Table<O> child, ForeignKey<O, PrekeyRecord> key) {
        super(child, key, PREKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Router.ROUTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PREKEY_PKEY, Indexes.PREKEY_PREKEY_KEY, Indexes.PREKEY_USER_ID_KEY_ID_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<PrekeyRecord, Long> getIdentity() {
        return Keys.IDENTITY_PREKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PrekeyRecord> getPrimaryKey() {
        return Keys.PREKEY_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PrekeyRecord>> getKeys() {
        return Arrays.<UniqueKey<PrekeyRecord>>asList(Keys.PREKEY_PKEY, Keys.PREKEY_USER_ID_KEY_ID_KEY, Keys.PREKEY_PREKEY_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<PrekeyRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<PrekeyRecord, ?>>asList(Keys.PREKEY__PREKEY_USER_ID_FKEY);
    }

    public CowpiUser cowpiUser() {
        return new CowpiUser(this, Keys.PREKEY__PREKEY_USER_ID_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Prekey as(String alias) {
        return new Prekey(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Prekey as(Name alias) {
        return new Prekey(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Prekey rename(String name) {
        return new Prekey(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Prekey rename(Name name) {
        return new Prekey(name, null);
    }
}
