CREATE TABLE minion (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(128) NOT NULL,
    master BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE friendship (
    minion BIGINT,
    friend BIGINT
);

CREATE TABLE toy (
    minion BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL
);

CREATE TABLE person (
    id BIGINT AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    PRIMARY KEY (id)
);

