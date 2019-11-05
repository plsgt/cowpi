CREATE TABLE conversation (
    id bigserial PRIMARY KEY,
    conversation_id bigint  NOT NULL UNIQUE,
    next_index bigint  NOT NULL
);

CREATE TABLE prekey (
    id bigserial PRIMARY KEY,
    private_key bytea  NOT NULL,
    public_key bytea  NOT NULL
);

CREATE TABLE participant_state (
    id bigserial PRIMARY KEY,
    username varchar(256) NOT NULL,
    longterm_key bytea  NOT NULL,
    conversation bigint  NOT NULL,
    remote_ephemeral_key_id bigint  NOT NULL,
    remote_ephemeral_key bytea  NOT NULL,
    UNIQUE(conversation, username),
    FOREIGN KEY (conversation) REFERENCES conversation(id)
);

CREATE TABLE local_key (
    id bigserial PRIMARY KEY,
    state bigint NOT NULL,
    local_ephemeral_key_id bigint  NOT NULL,
    local_ephemeral_priv_key bytea  NOT NULL,
    UNIQUE(state, local_ephemeral_key_id),
    FOREIGN KEY (state) REFERENCES participant_state(id)
);

CREATE TABLE router_mailbox (
    id bigserial PRIMARY KEY,
    conversation bigint NOT NULL,
    message_index bigint NOT NULL,
    message bytea NOT NULL,
    UNIQUE(conversation, message_index),
    FOREIGN KEY (conversation) REFERENCES conversation(id)
)
