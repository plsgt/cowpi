CREATE TABLE cowpi_user (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    longterm_key BLOB NOT NULL
);

CREATE TABLE prekey (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    key_id INTEGER NOT NULL,
    prekey BLOB NOT NULL UNIQUE,
    UNIQUE (user_id, key_id),
    FOREIGN KEY (user_id) REFERENCES grompi_user(id)
);