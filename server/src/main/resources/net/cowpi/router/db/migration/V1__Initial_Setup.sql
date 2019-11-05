CREATE TABLE cowpi_user (
    id bigserial PRIMARY KEY,
    username varchar(256) NOT NULL UNIQUE,
    longterm_key bytea NOT NULL
);

CREATE TABLE prekey (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    key_id bigint NOT NULL,
    prekey bytea NOT NULL UNIQUE,
    UNIQUE (user_id, key_id),
    FOREIGN KEY (user_id) REFERENCES cowpi_user(id)
);

CREATE TABLE conversation_message (
    id bigserial PRIMARY KEY,
    conversation_id bigint NOT NULL,
    message_index bigint NOT NULL,
    message bytea NOT NULL,
    oes_response_count integer NOT NULL default 0,
    UNIQUE (conversation_id, message_index)
);

CREATE TABLE mailbox (
    id bigserial PRIMARY KEY,
    user_to bigint NOT NULL,
    message bytea NOT NULL,
    FOREIGN KEY (user_to) REFERENCES cowpi_user(id)
);

CREATE TABLE oes_service (
    id bigserial PRIMARY KEY,
    oes_name varchar(256) NOT NULL UNIQUE
);

CREATE TABLE oes_prekey (
    id bigserial PRIMARY KEY,
    oes_id bigint NOT NULL,
    key_id bigint NOT NULL,
    prekey bytea NOT NULL UNIQUE,
    UNIQUE (oes_id, key_id),
    FOREIGN KEY (oes_id) REFERENCES oes_service(id)
);


CREATE TABLE oes_message (
    id bigserial PRIMARY KEY,
    oes_service bigint NOT NULL,
    conversation_message bigint NOT NULL,
    outgoing bytea NOT NULL,
    message bytea,
    UNIQUE (conversation_message, oes_service),
    FOREIGN KEY (oes_service) REFERENCES oes_service(id),
    FOREIGN KEY (conversation_message) REFERENCES conversation_message(id)
);

INSERT INTO oes_service ( oes_name )
VALUES ('oes_1'), ('oes_2');