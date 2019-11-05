/*
 * This file is generated by jOOQ.
 */
package net.cowpi.server.jooq;


import javax.annotation.Generated;

import net.cowpi.server.jooq.tables.ConversationMessage;
import net.cowpi.server.jooq.tables.CowpiUser;
import net.cowpi.server.jooq.tables.Mailbox;
import net.cowpi.server.jooq.tables.OesMessage;
import net.cowpi.server.jooq.tables.OesPrekey;
import net.cowpi.server.jooq.tables.OesService;
import net.cowpi.server.jooq.tables.Prekey;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables of the <code>router</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index CONVERSATION_MESSAGE_CONVERSATION_ID_MESSAGE_INDEX_KEY = Indexes0.CONVERSATION_MESSAGE_CONVERSATION_ID_MESSAGE_INDEX_KEY;
    public static final Index CONVERSATION_MESSAGE_PKEY = Indexes0.CONVERSATION_MESSAGE_PKEY;
    public static final Index COWPI_USER_PKEY = Indexes0.COWPI_USER_PKEY;
    public static final Index COWPI_USER_USERNAME_KEY = Indexes0.COWPI_USER_USERNAME_KEY;
    public static final Index MAILBOX_PKEY = Indexes0.MAILBOX_PKEY;
    public static final Index OES_MESSAGE_CONVERSATION_MESSAGE_OES_SERVICE_KEY = Indexes0.OES_MESSAGE_CONVERSATION_MESSAGE_OES_SERVICE_KEY;
    public static final Index OES_MESSAGE_PKEY = Indexes0.OES_MESSAGE_PKEY;
    public static final Index OES_PREKEY_OES_ID_KEY_ID_KEY = Indexes0.OES_PREKEY_OES_ID_KEY_ID_KEY;
    public static final Index OES_PREKEY_PKEY = Indexes0.OES_PREKEY_PKEY;
    public static final Index OES_PREKEY_PREKEY_KEY = Indexes0.OES_PREKEY_PREKEY_KEY;
    public static final Index OES_SERVICE_OES_NAME_KEY = Indexes0.OES_SERVICE_OES_NAME_KEY;
    public static final Index OES_SERVICE_PKEY = Indexes0.OES_SERVICE_PKEY;
    public static final Index PREKEY_PKEY = Indexes0.PREKEY_PKEY;
    public static final Index PREKEY_PREKEY_KEY = Indexes0.PREKEY_PREKEY_KEY;
    public static final Index PREKEY_USER_ID_KEY_ID_KEY = Indexes0.PREKEY_USER_ID_KEY_ID_KEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index CONVERSATION_MESSAGE_CONVERSATION_ID_MESSAGE_INDEX_KEY = Internal.createIndex("conversation_message_conversation_id_message_index_key", ConversationMessage.CONVERSATION_MESSAGE, new OrderField[] { ConversationMessage.CONVERSATION_MESSAGE.CONVERSATION_ID, ConversationMessage.CONVERSATION_MESSAGE.MESSAGE_INDEX }, true);
        public static Index CONVERSATION_MESSAGE_PKEY = Internal.createIndex("conversation_message_pkey", ConversationMessage.CONVERSATION_MESSAGE, new OrderField[] { ConversationMessage.CONVERSATION_MESSAGE.ID }, true);
        public static Index COWPI_USER_PKEY = Internal.createIndex("cowpi_user_pkey", CowpiUser.COWPI_USER, new OrderField[] { CowpiUser.COWPI_USER.ID }, true);
        public static Index COWPI_USER_USERNAME_KEY = Internal.createIndex("cowpi_user_username_key", CowpiUser.COWPI_USER, new OrderField[] { CowpiUser.COWPI_USER.USERNAME }, true);
        public static Index MAILBOX_PKEY = Internal.createIndex("mailbox_pkey", Mailbox.MAILBOX, new OrderField[] { Mailbox.MAILBOX.ID }, true);
        public static Index OES_MESSAGE_CONVERSATION_MESSAGE_OES_SERVICE_KEY = Internal.createIndex("oes_message_conversation_message_oes_service_key", OesMessage.OES_MESSAGE, new OrderField[] { OesMessage.OES_MESSAGE.CONVERSATION_MESSAGE, OesMessage.OES_MESSAGE.OES_SERVICE }, true);
        public static Index OES_MESSAGE_PKEY = Internal.createIndex("oes_message_pkey", OesMessage.OES_MESSAGE, new OrderField[] { OesMessage.OES_MESSAGE.ID }, true);
        public static Index OES_PREKEY_OES_ID_KEY_ID_KEY = Internal.createIndex("oes_prekey_oes_id_key_id_key", OesPrekey.OES_PREKEY, new OrderField[] { OesPrekey.OES_PREKEY.OES_ID, OesPrekey.OES_PREKEY.KEY_ID }, true);
        public static Index OES_PREKEY_PKEY = Internal.createIndex("oes_prekey_pkey", OesPrekey.OES_PREKEY, new OrderField[] { OesPrekey.OES_PREKEY.ID }, true);
        public static Index OES_PREKEY_PREKEY_KEY = Internal.createIndex("oes_prekey_prekey_key", OesPrekey.OES_PREKEY, new OrderField[] { OesPrekey.OES_PREKEY.PREKEY }, true);
        public static Index OES_SERVICE_OES_NAME_KEY = Internal.createIndex("oes_service_oes_name_key", OesService.OES_SERVICE, new OrderField[] { OesService.OES_SERVICE.OES_NAME }, true);
        public static Index OES_SERVICE_PKEY = Internal.createIndex("oes_service_pkey", OesService.OES_SERVICE, new OrderField[] { OesService.OES_SERVICE.ID }, true);
        public static Index PREKEY_PKEY = Internal.createIndex("prekey_pkey", Prekey.PREKEY, new OrderField[] { Prekey.PREKEY.ID }, true);
        public static Index PREKEY_PREKEY_KEY = Internal.createIndex("prekey_prekey_key", Prekey.PREKEY, new OrderField[] { Prekey.PREKEY.PREKEY_ }, true);
        public static Index PREKEY_USER_ID_KEY_ID_KEY = Internal.createIndex("prekey_user_id_key_id_key", Prekey.PREKEY, new OrderField[] { Prekey.PREKEY.USER_ID, Prekey.PREKEY.KEY_ID }, true);
    }
}