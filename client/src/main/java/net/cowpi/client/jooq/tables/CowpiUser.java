/*
 * This file is generated by jOOQ.
 */
package net.cowpi.client.jooq.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import net.cowpi.client.jooq.DefaultSchema;
import net.cowpi.client.jooq.Indexes;
import net.cowpi.client.jooq.Keys;
import net.cowpi.client.jooq.tables.records.CowpiUserRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
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
public class CowpiUser extends TableImpl<CowpiUserRecord> {

    private static final long serialVersionUID = -2081481378;

    /**
     * The reference instance of <code>cowpi_user</code>
     */
    public static final CowpiUser COWPI_USER = new CowpiUser();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CowpiUserRecord> getRecordType() {
        return CowpiUserRecord.class;
    }

    /**
     * The column <code>cowpi_user.id</code>.
     */
    public final TableField<CowpiUserRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>cowpi_user.username</code>.
     */
    public final TableField<CowpiUserRecord, String> USERNAME = createField("username", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>cowpi_user.longterm_key</code>.
     */
    public final TableField<CowpiUserRecord, byte[]> LONGTERM_KEY = createField("longterm_key", org.jooq.impl.SQLDataType.BLOB.nullable(false), this, "");

    /**
     * Create a <code>cowpi_user</code> table reference
     */
    public CowpiUser() {
        this(DSL.name("cowpi_user"), null);
    }

    /**
     * Create an aliased <code>cowpi_user</code> table reference
     */
    public CowpiUser(String alias) {
        this(DSL.name(alias), COWPI_USER);
    }

    /**
     * Create an aliased <code>cowpi_user</code> table reference
     */
    public CowpiUser(Name alias) {
        this(alias, COWPI_USER);
    }

    private CowpiUser(Name alias, Table<CowpiUserRecord> aliased) {
        this(alias, aliased, null);
    }

    private CowpiUser(Name alias, Table<CowpiUserRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> CowpiUser(Table<O> child, ForeignKey<O, CowpiUserRecord> key) {
        super(child, key, COWPI_USER);
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
        return Arrays.<Index>asList(Indexes.SQLITE_AUTOINDEX_COWPI_USER_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<CowpiUserRecord> getPrimaryKey() {
        return Keys.PK_COWPI_USER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<CowpiUserRecord>> getKeys() {
        return Arrays.<UniqueKey<CowpiUserRecord>>asList(Keys.PK_COWPI_USER, Keys.SQLITE_AUTOINDEX_COWPI_USER_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CowpiUser as(String alias) {
        return new CowpiUser(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CowpiUser as(Name alias) {
        return new CowpiUser(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public CowpiUser rename(String name) {
        return new CowpiUser(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CowpiUser rename(Name name) {
        return new CowpiUser(name, null);
    }
}